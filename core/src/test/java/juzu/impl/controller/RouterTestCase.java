package juzu.impl.controller;

import juzu.impl.application.ApplicationDescriptor;
import juzu.impl.controller.descriptor.ControllerDescriptor;
import juzu.impl.controller.descriptor.ControllerRoute;
import juzu.impl.inject.spi.InjectImplementation;
import juzu.test.AbstractInjectTestCase;
import juzu.test.protocol.mock.MockApplication;
import org.junit.Test;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class RouterTestCase extends AbstractInjectTestCase {

  public RouterTestCase(InjectImplementation di) {
    super(di);
  }

  @Test
  public void testDeclaration() throws Exception {
    MockApplication<?> application = application("controller", "router", "declaration").init();

    //
    ApplicationDescriptor descriptor = application.getContext().getDescriptor();

    ControllerDescriptor controller = descriptor.getController();

    List<ControllerRoute> routes = controller.getRoutes();
    assertEquals(3, routes.size());



  }
}
