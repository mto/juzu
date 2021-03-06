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

package inject.declared.bean.qualifier.declared;

import inject.AbstractInjectTestCase;
import inject.Color;
import inject.ColorizedLiteral;
import juzu.impl.inject.spi.InjectorProvider;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class DeclaredQualifierDeclaredBeanTestCase<B, I> extends AbstractInjectTestCase<B, I> {

  public DeclaredQualifierDeclaredBeanTestCase(InjectorProvider di) {
    super(di);
  }

  @Test
  public void test() throws Exception {
    init();
    bootstrap.declareBean(Injected.class, null, null, null);
    bootstrap.declareBean(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.BLUE)), null);
    bootstrap.declareBean(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.RED)), null);
    bootstrap.declareBean(Bean.class, null, Collections.<Annotation>singleton(new ColorizedLiteral(Color.GREEN)), Bean.Green.class);
    boot();

    //
    Injected injected = getBean(Injected.class);
    assertNotNull(injected);
    assertNotNull(injected.blue);
    assertNotNull(injected.red);
    assertNotNull(injected.green);
    assertNotSame(injected.blue.getId(), injected.red.getId());
    assertNotSame(injected.green.getId(), injected.red.getId());
    assertNotSame(injected.blue.getId(), injected.green.getId());
    assertInstanceOf(Bean.class, injected.blue);
    assertInstanceOf(Bean.class, injected.red);
    assertInstanceOf(Bean.Green.class, injected.green);
    assertNotInstanceOf(Bean.Green.class, injected.blue);
    assertNotInstanceOf(Bean.Green.class, injected.red);
  }
}
