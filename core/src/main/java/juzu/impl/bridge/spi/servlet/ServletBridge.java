/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package juzu.impl.bridge.spi.servlet;

import juzu.Response;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.spi.web.Handler;
import juzu.impl.common.Formatting;
import juzu.impl.common.Tools;
import juzu.impl.common.Logger;
import juzu.impl.common.SimpleMap;
import juzu.impl.compiler.CompilationException;
import juzu.impl.plugin.module.Module;
import juzu.impl.plugin.module.ModuleContext;
import juzu.impl.resource.ResourceResolver;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletBridge extends HttpServlet {

  /** . */
  Module module;

  /** . */
  Logger log;

  /** . */
  private String path;

  /** . */
  private BridgeConfig config;

  /** . */
  private Bridge bridge;

  /** . */
  private Handler handler;

  @Override
  public void init() throws ServletException {

    //
    String path = null;
    ServletRegistration reg = getServletContext().getServletRegistration(getServletName());
    for (String mapping : reg.getMappings()) {
      if ("/".equals(mapping)) {
        path = "";
        break;
      } else if ("/*".equals(mapping)) {
        throw new UnsupportedOperationException("Implement me");
      } else if (mapping.endsWith("/*")) {
        path = mapping.substring(0, mapping.length() - 2);
      } else {
        throw new UnsupportedOperationException("Should not be possible");
      }
    }
    if (path == null) {
      throw new ServletException("Juzu servlet should be mounted on an url pattern");
    }

    //
    final ServletConfig servletConfig = getServletConfig();

    //
    Logger log = new Logger() {
      public void log(CharSequence msg) {
        System.out.println("[" + servletConfig.getServletName() + "] " + msg);
      }

      public void log(CharSequence msg, Throwable t) {
        System.err.println("[" + servletConfig.getServletName() + "] " + msg);
        t.printStackTrace();
      }
    };

    //
    BridgeConfig config;
    try {
      config = new BridgeConfig(new SimpleMap<String, String>() {
        @Override
        protected Iterator<String> keys() {
          return BridgeConfig.NAMES.iterator();
        }
        @Override
        public String get(Object key) {
          if (BridgeConfig.APP_NAME.equals(key)) {
            return getApplicationName(servletConfig);
          } else if (BridgeConfig.INJECT.equals(key)) {
            // Cascade:
            // 1/ portlet init param
            // 2/ serlvet context init param
            String inject = servletConfig.getInitParameter((String)key);
            if (inject == null) {
              inject = servletConfig.getServletContext().getInitParameter((String)key);
            }
            return inject;
          } else if (BridgeConfig.NAMES.contains(key)) {
            return servletConfig.getInitParameter((String)key);
          } else {
            return null;
          }
        }
      });
    }
    catch (Exception e) {
      throw wrap(e);
    }

    //
    if (config.name == null) {
      throw new ServletException("No application configured");
    }

    //
    this.log = log;
    this.config = config;
    this.handler = null;
    this.path = path;
  }

  static ServletException wrap(Throwable e) {
    return e instanceof ServletException ? (ServletException)e : new ServletException("Could not find an application to start", e);
  }

  /**
   * Returns the application name to use using the <code>juzu.app_name</code> init parameter of the portlet deployment
   * descriptor. Subclass can override it to provide a custom application name.
   *
   * @param config the portlet config
   * @return the application name
   */
  protected String getApplicationName(ServletConfig config) {
    return config.getInitParameter("juzu.app_name");
  }

  private void refresh() throws Exception {

    //
    if (module == null) {
      module = (Module)getServletContext().getAttribute("juzu.module");
      if (module == null) {
        try {
          ModuleContext moduleContext = new ServletModuleContext(getServletContext());
          getServletContext().setAttribute("juzu.module", module = new Module(moduleContext));
        }
        catch (Exception e) {
          throw wrap(e);
        }
      }
      module.lease();
    }

    // Get asset server
    AssetServer server = (AssetServer)getServletContext().getAttribute("asset.server");
    if (server == null) {
      server = new AssetServer();
      getServletContext().setAttribute("asset.server", server);
    }

    //
    if (bridge == null) {
      // Create and configure bridge
      bridge = new Bridge(
          log,
          module,
          this.config,
          module.context.getResourcePath(),
          server,
          new ResourceResolver() {
            public URL resolve(String uri) {
              try {
                return getServletConfig().getServletContext().getResource(uri);
              }
              catch (MalformedURLException e) {
                return null;
              }
            }
          });
    }

    //
    boolean stale = bridge.refresh();
    if (stale) {
      if (handler != null) {
        Tools.safeClose(handler);
        handler = null;
      }
    }

    //
    if (handler == null) {
      this.handler = new Handler(bridge);
    }
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    //
    ServletWebBridge bridge = new ServletWebBridge(req, resp, path);

    // Do we need to send a server resource ?
    if (bridge.getRequestPath().length() > 1 && !bridge.getRequestPath().startsWith("/WEB-INF/")) {
      URL url = getServletContext().getResource(bridge.getRequestPath());
      if (url != null) {
        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
        dispatcher.include(bridge.getRequest(), bridge.getResponse());
        return;
      }
    }

    //
    try {
      refresh();
    }
    catch (CompilationException e) {
      StringWriter writer = new StringWriter();
      PrintWriter printer = new PrintWriter(writer);
      Formatting.renderErrors(printer, e.getErrors());
      bridge.send(Response.error(writer.getBuffer().toString()), true);
      return;
    }
    catch (Exception e) {
      throw wrap(e);
    }

    //
    try {
      handler.handle(bridge);
    }
    catch (Throwable throwable) {
      throw wrap(throwable);
    }
  }

  @Override
  public void destroy() {
    if (module != null) {
      if (module.release()) {
        // Should dispose module (todo later)
      }
    }
    if (handler != null) {
      Tools.safeClose(handler);
      this.handler = null;
    }
  }
}
