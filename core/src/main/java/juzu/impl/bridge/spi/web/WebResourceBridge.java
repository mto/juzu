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

package juzu.impl.bridge.spi.web;

import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.impl.request.Method;
import juzu.request.ClientContext;

import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class WebResourceBridge extends WebMimeBridge implements ResourceBridge {

  WebResourceBridge(
      Bridge bridge,
      Handler handler,
      WebBridge webBridge,
      Method<?> target,
      Map<String, String[]> parameters) {
    super(bridge, handler, webBridge, target, parameters);
  }

  public ClientContext getClientContext() {
    return http.getClientContext();
  }
}
