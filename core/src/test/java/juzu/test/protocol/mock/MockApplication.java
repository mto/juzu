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

package juzu.test.protocol.mock;

import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.fs.spi.ReadWriteFileSystem;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.application.ApplicationLifeCycle;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.Logger;
import juzu.impl.plugin.module.ModuleLifeCycle;
import juzu.impl.resource.ResourceResolver;
import juzu.request.ApplicationContext;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class MockApplication<P> implements Closeable, ApplicationContext {

  /** . */
  private static final ResourceBundle.Control control = new ResourceBundle.Control() {};

  /** . */
  private final HashMap<Locale, ResourceBundleImpl> bundles = new HashMap<Locale, ResourceBundleImpl>();

  /** . */
  final ClassLoader classLoader;

  /** . */
  private final ApplicationLifeCycle<P, ?> lifeCycle;

  /** . */
  private final ReadWriteFileSystem<P> classes;

  public <L> MockApplication(
      ReadWriteFileSystem<P> classes,
      ClassLoader classLoader,
      InjectorProvider implementation,
      Name name) throws Exception {

    /** . */
    Logger log = new Logger() {
      public void log(CharSequence msg) {
//        System.out.println("[" + name + "] " + msg);
        System.out.println("[" + "] " + msg);
      }

      public void log(CharSequence msg, Throwable t) {
//        System.err.println("[" + name + "] " + msg);
        System.err.println("[" + "] " + msg);
        t.printStackTrace(System.err);
      }
    };

    //
    ModuleLifeCycle<P> module = new ModuleLifeCycle.Static<P>(log, classLoader, classes);

    //
    ApplicationLifeCycle<P, P> lifeCycle = new ApplicationLifeCycle<P, P>(
        log,
        module,
        implementation,
        name,
        classes,
        null,
        new ResourceResolver() {
          public URL resolve(String uri) {
            return null;
          }
        });

    //
    this.classes = classes;
    this.classLoader = classLoader;
    this.lifeCycle = lifeCycle;
  }

  public ReadFileSystem<P> getClasses() {
    return classes;
  }

  public MockApplication<P> init() throws Exception {
    lifeCycle.refresh();
    return this;
  }

  public ApplicationLifeCycle<P, ?> getLifeCycle() {
    return lifeCycle;
  }

  public Application getContext() {
    return lifeCycle.getApplication();
  }

  void invoke(RequestBridge bridge) {
    lifeCycle.getApplication().invoke(bridge);
  }

  public MockClient client() {
    return new MockClient(this);
  }

  public void close() throws IOException {
    Tools.safeClose(lifeCycle);
  }

  public ResourceBundle resolveBundle(Locale locale) {
    ResourceBundle bundle = null;
    for (Locale current = locale;current != null;current = control.getFallbackLocale("whatever", current)) {
      bundle = bundles.get(current);
      if (bundle != null) {
        break;
      }
    }
    return bundle;
  }

  public void addMessage(Locale locale, String key, String value) {
    ResourceBundleImpl bundle = bundles.get(locale);
    if (bundle == null) {
      bundles.put(locale, bundle = new ResourceBundleImpl());
    }
    bundle.messages.put(key, value);
  }

  private static class ResourceBundleImpl extends ResourceBundle {

    /** . */
    private final HashMap<String, String> messages = new HashMap<String, String>();

    @Override
    protected Object handleGetObject(String key) {
      return messages.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
      return Collections.enumeration(messages.keySet());
    }
  }
}
