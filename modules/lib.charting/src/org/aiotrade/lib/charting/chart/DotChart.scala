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
package org.aiotrade.lib.charting.chart

import org.aiotrade.lib.charting.widget.DiamondDot
import org.aiotrade.lib.charting.widget.WidgetModel
import org.aiotrade.lib.math.timeseries.Var
import org.aiotrade.lib.charting.laf.LookFeel
import org.aiotrade.lib.charting.widget.HeavyPathWidget

/**
 *
 * @author Caoyuan Deng
 */
class DotChart extends AbstractChart {
  final class Model extends WidgetModel {
    var v: Var[_] = _
        
    def set(v: Var[_]) {
      this.v = v
    }
  }

  type M = Model
    
  protected def createModel = new Model
    
  protected def plotChart {
    val m = model
    val color = LookFeel.getCurrent.getChartColor(getDepth)
    setForeground(color)
        
    val heavyPathWidget = addChild(new HeavyPathWidget)
    val template = new DiamondDot
    var bar = 1
    while (bar <= nBars) {
            
      var i = 0
      while (i < nBarsCompressed) {
        val time = tb(bar + i)
        val item = ser.getItem(time)
                
        if (item != null) {
          val value = item.getFloat(model.v)
                    
          if (! value.isNaN) {
            template.model.set(xb(bar), yv(value), wBar, false)
            template.setForeground(color)
            template.plot;
            heavyPathWidget.appendFrom(template)
          }
        }

        i += 1
      }

      bar += nBarsCompressed
    }
        
  }
    
}


