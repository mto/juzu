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

package juzu.plugin.upload;

import juzu.test.AbstractWebTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractUploadTestCase extends AbstractWebTestCase {

  /** . */
  public static String contentType;

  /** . */
  public static String content;

  /** . */
  public static String text;

  @Drone
  WebDriver driver;

  protected abstract URL getURL();

  @Test
  public void testUpload() throws Exception {
    driver.get(getURL().toString());
    WebElement submit = driver.findElement(By.id("submit"));
    WebElement file = driver.findElement(By.id("file"));
    WebElement text = driver.findElement(By.id("text"));
    File f = File.createTempFile("juzu", ".txt");
    f.deleteOnExit();
    FileWriter writer = new FileWriter(f);
    writer.write("HELLO");
    writer.close();
    file.sendKeys(f.getAbsolutePath());
    text.sendKeys("text_value");
    AbstractUploadTestCase.contentType = null;
    AbstractUploadTestCase.content = null;
    AbstractUploadTestCase.text = null;
    submit.submit();
    assertEquals("text/plain", AbstractUploadTestCase.contentType);
    assertEquals("HELLO", AbstractUploadTestCase.content);
    assertEquals("text_value", AbstractUploadTestCase.text);
  }
}
