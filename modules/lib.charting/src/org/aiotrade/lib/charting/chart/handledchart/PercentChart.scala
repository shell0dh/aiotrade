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
package org.aiotrade.lib.charting.chart.handledchart

import org.aiotrade.lib.charting.widget.WidgetModel
import org.aiotrade.lib.charting.chart.AbstractChart
import org.aiotrade.lib.charting.laf.LookFeel
import org.aiotrade.lib.charting.widget.Label
import org.aiotrade.lib.charting.widget.PathWidget


/**
 *
 * @author Caoyuan Deng
 */
class PercentChart extends AbstractChart {
  final class Model extends WidgetModel {
    var t1: Long = _
    var v1: Float = _
    var t2: Long = _
    var v2: Float = _
        
    def set(t1: Long, v1: Float, t2: Long, v2: Float) {
      this.t1 = t1
      this.v1 = v1
      this.t2 = t2
      this.v2 = v2
    }
  }

  type M = Model
    
  protected def createModel = new Model
    
  protected def plotChart {
    val m = model
        
    val color = LookFeel.getCurrent.drawingColor
    setForeground(color)
        
    val xs = new Array[Float](2)
    val ys = new Array[Float](2)
    xs(0) = xb(bt(m.t1))
    xs(1) = xb(bt(m.t2))
    ys(0) = yv(m.v1)
    ys(1) = yv(m.v2)
    val k = if (xs(1) - xs(0) == 0) 1F else (ys(1) - ys(0)) / (xs(1) - xs(0))
    val interval = ys(1) - ys(0)
    val xmin = Math.min(xs(0), xs(1))
    val xmax = Math.max(xs(0), xs(1))
        
    val y0 = ys(0);
    val y1 = ys(0) + interval * 0.25f
    val y2 = ys(0) + interval * 0.333333f
    val y3 = ys(0) + interval * 0.5f
    val y4 = ys(0) + interval * 0.666667f
    val y5 = ys(0) + interval * 0.75f
    val y6 = ys(1)
        
    val label1 = addChild(new Label)
    label1.setFont(LookFeel.getCurrent.axisFont)
    label1.setForeground(color)
    label1.model.set(xs(0), y1 - 2, "25.0%")
    label1.plot
        
    val label2 = addChild(new Label)
    label2.setFont(LookFeel.getCurrent.axisFont)
    label2.setForeground(color)
    label2.model.set(xs(0), y2 - 2, "33.3%")
    label2.plot
        
    val label3 = addChild(new Label)
    label3.setFont(LookFeel.getCurrent.axisFont)
    label3.setForeground(color)
    label3.model.set(xs(0), y3 - 2, "50.0%");
    label3.plot
        
    val label4 = addChild(new Label)
    label4.setFont(LookFeel.getCurrent.axisFont)
    label4.setForeground(color)
    label4.model.set(xs(0), y4 - 2, "66.7%")
    label4.plot

    val label5 = addChild(new Label)
    label5.setFont(LookFeel.getCurrent.axisFont)
    label5.setForeground(color)
    label5.model.set(xs(0), y5 - 2, "75.0%")
    label5.plot
        
    val pathWidget = addChild(new PathWidget)
    pathWidget.setForeground(color)
    val path = pathWidget.getPath

    var x1 = xb(0)
    var bar = 1
    while (bar <= nBars) {
            
      val x2 = xb(bar)
      if (x2 >= xmin && x2 <= xmax) {
        path.moveTo(x1, y0)
        path.lineTo(x2, y0)
        path.moveTo(x1, y1)
        path.lineTo(x2, y1)
        path.moveTo(x1, y2)
        path.lineTo(x2, y2)
        path.moveTo(x1, y3)
        path.lineTo(x2, y3)
        path.moveTo(x1, y4)
        path.lineTo(x2, y4)
        path.moveTo(x1, y5)
        path.lineTo(x2, y5)
        path.moveTo(x1, y6)
        path.lineTo(x2, y6)
      }
      /** should avoid the 1 point intersect at the each path's end point,
       * especially in XOR mode */
      x1 = x2 + 1

      bar += nBarsCompressed
    }
        
  }
    
}


