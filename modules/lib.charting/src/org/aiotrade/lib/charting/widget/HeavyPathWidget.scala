/*
 * Copyright (c) 2006-2007, AIOTrade Computing Co. and Contributors
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *    
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *    
 *  o Neither the name of AIOTrade Computing Co. nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.aiotrade.lib.charting.widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;import scala.collection.mutable.HashMap


/**
 *
 * @author  Caoyuan Deng
 * @version 1.0, November 27, 2006, 7:38 AM
 * @since   1.0.4
 */
class HeavyPathWidget extends AbstractWidget {
    
  private val colorsWithPath = new HashMap[Color, GeneralPath]
    
  protected def createModel: M = null
    
  override def isContainerOnly: Boolean = {
    false
  }
    
  override protected def makePreferredBounds: Rectangle = {
    val pathBounds = new Rectangle
    for (path <- colorsWithPath.values) {
      pathBounds.add(path.getBounds)
    }
        
    return new Rectangle(
      pathBounds.x, pathBounds.y,
      pathBounds.width + 1, pathBounds.height + 1)
  }
    
  def getPath(color: Color): GeneralPath = {
    colorsWithPath.get(color) match {
      case None =>
        val pathx = borrowPath
        colorsWithPath.put(color, pathx)
        pathx
      case Some(x) => x  
    }
  }
    
  def appendFrom(pathWidget: PathWidget) {
    val color = pathWidget.getForeground
    getPath(color).append(pathWidget.getPath, false)
  }
    
  override protected def widgetContains(x: Double, y: Double, width: Double, height: Double): Boolean = {
    for (path <- colorsWithPath.values) {
      if (path.contains(x, y, width, height)) {
        return true
      }
    }
    false
  }
    
  override def widgetIntersects(x: Double, y: Double, width: Double, height: Double): Boolean = {
    for (path <- colorsWithPath.values) {
      if (path.intersects(x, y, width, height)) {
        return true;
      }
    }
    return false;
  }
    
  override def renderWidget(g0: Graphics) {
    val g = g0.asInstanceOf[Graphics2D]
        
    for (color <- colorsWithPath.keySet) {
      g.setColor(color)
      g.draw(colorsWithPath.get(color).get)
    }
  }
    
  override def reset {
    super.reset
    for (path <- colorsWithPath.values) {
      path.reset
    }
  }
    
  override protected def plotWidget {
  }

  @throws(classOf[Throwable])
  override protected def finalize {
    for (path <- colorsWithPath.values) {
      returnPath(path)
    }
        
    super.finalize
  }
    
}

