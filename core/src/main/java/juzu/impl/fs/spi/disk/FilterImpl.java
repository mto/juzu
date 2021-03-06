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

package juzu.impl.fs.spi.disk;

import juzu.impl.common.Name;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class FilterImpl implements FilenameFilter {

  /** . */
  private final Map<File, String> valids;

  FilterImpl(File root, Name path) {
    Map<File, String> valids = new HashMap<File, String>();

    {
      File current = root;
      for (String name : path) {
        valids.put(current, name);
        current = new File(current, name);
      }
    }


    this.valids = valids;
  }

  public boolean accept(File dir, String name) {
    String found = valids.get(dir);
    return found == null || found.equals(name);
  }
}
