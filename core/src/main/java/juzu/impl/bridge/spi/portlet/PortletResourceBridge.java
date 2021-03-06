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

package juzu.impl.bridge.spi.portlet;

import juzu.Response;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.spi.ResourceBridge;
import juzu.request.ClientContext;
import juzu.request.Phase;

import javax.portlet.PortletConfig;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import java.io.IOException;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class PortletResourceBridge extends PortletMimeBridge<ResourceRequest, ResourceResponse> implements ResourceBridge {

  /** . */
  private final PortletClientContext clientContext;

  public PortletResourceBridge(Bridge bridge, ResourceRequest request, ResourceResponse response, PortletConfig config) {
    super(bridge, request, response, config);

    //
    this.clientContext = new PortletClientContext(request);
  }

  @Override
  protected Phase getPhase() {
    return Phase.RESOURCE;
  }

  public ClientContext getClientContext() {
    return clientContext;
  }

  @Override
  protected void sendProperties() throws IOException {
    if (response instanceof Response.Content) {
      Response.Content resource = (Response.Content)response;
      int status = resource.getStatus();
      if (status != 200) {
        resp.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(status));
      }
      super.setResponse(response);
    }
  }
}
