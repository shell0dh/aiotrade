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
package org.aiotrade.lib.dataserver.yahoo

import java.awt.Image
import java.io.{BufferedReader, File, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.text.{DateFormat, ParseException, SimpleDateFormat}
import java.util.{Calendar, Date, Locale, TimeZone}
import java.util.zip.GZIPInputStream
import javax.imageio.ImageIO
import org.aiotrade.lib.math.timeseries.TFreq
import org.aiotrade.lib.securities.{Exchange, Quote}
import org.aiotrade.lib.securities.dataserver.{QuoteContract, QuoteServer}
import org.aiotrade.lib.util.collection.ArrayList
import scala.annotation.tailrec

/**
 * This class will load the quote datas from data source to its data storage: quotes.
 * @TODO it will be implemented as a Data Server ?
 *
 * @author Caoyuan Deng
 */
object YahooQuoteServer {
  // * "http://table.finance.yahoo.com/table.csv"
  protected val BaseUrl = "http://table.finance.yahoo.com"
  protected val UrlPath = "/table.csv"
  protected val dateFormat: DateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US)

  def exchangeOf(symbol: String): Exchange = {
    symbol.split("\\.") match {
      case Array(head, exchange) => exchange.toUpperCase match {
          case "L"  => Exchange.L
          case "SS" => Exchange.SS
          case "SZ" => Exchange.SZ
          case _ => Exchange.N
        }
      case _ => Exchange.N
    }
  }

}

import YahooQuoteServer._
class YahooQuoteServer extends QuoteServer {

  private var contract: QuoteContract = _
  private var gzipped = false

  protected def connect: Boolean = true

  /**
   * Template:
   * http://table.finance.yahoo.com/table.csv?s=^HSI&a=01&b=20&c=1990&d=07&e=18&f=2005&g=d&ignore=.csv
   */
  @throws(classOf[Exception])
  protected def request {
    val cal = Calendar.getInstance

    contract = currentContract match {
      case Some(x: QuoteContract) => x
      case _ => return
    }

    val (begDate, endDate ) = if (fromTime <= ANCIENT_TIME /* @todo */) {
      (contract.beginDate, contract.endDate)
    } else {
      cal.setTimeInMillis(fromTime)
      (cal.getTime, new Date)
    }

    cal.setTime(begDate)
    val a = cal.get(Calendar.MONTH)
    val b = cal.get(Calendar.DAY_OF_MONTH)
    val c = cal.get(Calendar.YEAR)

    cal.setTime(endDate)
    val d = cal.get(Calendar.MONTH)
    val e = cal.get(Calendar.DAY_OF_MONTH)
    val f = cal.get(Calendar.YEAR)

    val urlStr = new StringBuilder(50)
    urlStr.append(BaseUrl).append(UrlPath)
    urlStr.append("?s=").append(contract.symbol)

    /** a, d is month, which from 0 to 11 */
    urlStr.append("&a=" + a + "&b=" + b + "&c=" + c +
                  "&d=" + d + "&e=" + e + "&f=" + f)

    urlStr.append("&g=d&ignore=.csv")

    val url = new URL(urlStr.toString)

    println(url)

    if (url != null) {
      val conn = url.openConnection.asInstanceOf[HttpURLConnection]
      conn.setRequestProperty("Accept-Encoding", "gzip")
      conn.setAllowUserInteraction(true)
      conn.setRequestMethod("GET")
      conn.setInstanceFollowRedirects(true)
      conn.connect

      val encoding = conn.getContentEncoding
      gzipped = if (encoding != null && encoding.indexOf("gzip") != -1) {
        true
      } else false
            
      inputStream = conn.getInputStream match {
        case null => None
        case is => Some(is)
      }
    }
  }

  /**
   * @return readed time
   */
  @throws(classOf[Exception])
  protected def read: Long =  {
    val is = inputStream match {
      case None => return 0
      case Some(x) => x
    }

    val reader = if (gzipped) {
      new BufferedReader(new InputStreamReader(new GZIPInputStream(is)))
    } else {
      new BufferedReader(new InputStreamReader(is))
    }

    /** skip first line */
    val s = reader.readLine

    resetCount
    val storage = storageOf(contract)
    val symbol = contract.symbol
    val exchange = exchangeOf(contract.symbol)
    val timeZone = exchange.timeZone
    // * for daily quote, yahoo returns exchange's local date, so use exchange time zone
    val cal = Calendar.getInstance(timeZone)
    val dateFormat = dateFormatOf(timeZone)
    
    @tailrec
    def loop(newestTime: Long): Long = reader.readLine match {
      case null => newestTime // break now
      case line => line.split(",") match {
          case Array(dateTimeX, openX, highX, lowX, closeX, volumeX, adjCloseX, _*) =>
            /**
             * !NOTICE
             * must catch the date parse exception, other wise, it's dangerous
             * for build a calendarTimes in MasterSer
             */
            try {
              val date = dateFormat.parse(dateTimeX.trim)
              cal.clear
              cal.setTime(date)
            } catch {
              case _: ParseException => loop(newestTime)
            }
                    
            var time = cal.getTimeInMillis
            if (time < fromTime) {
              loop(newestTime)
            }

            // quote time is rounded to 00:00, we should adjust it to open time
            time += exchange.openTimeOfDay

            val quote = new Quote

            quote.time   = time
            quote.open   = openX.trim.toFloat
            quote.high   = highX.trim.toFloat
            quote.low    = lowX.trim.toFloat
            quote.close  = closeX.trim.toFloat
            quote.volume = volumeX.trim.toFloat / 100f
            quote.amount = -1
            quote.close_adj = adjCloseX.trim.toFloat

            val newestTime1 = if (quote.high * quote.low * quote.close == 0) {
              newestTime
            } else {
              // @Note: have to asIntstanceOf[ArrayList[Quote]] explicit, otherwise, compiler will throw:
              // exception when typing storage.+=
              // illegal cyclic reference involving class ArrayList in file /Users/dcaoyuan/myprjs/aiotrade.sf/opensource/libs/lib.dataserver.yahoo/src/main/scala/org/aiotrade/lib/dataserver/yahoo/YahooQuoteServer.scala
              // Don't know why. but {{{storage + quote}}} works
              storage.asInstanceOf[ArrayList[Quote]] += quote
              countOne
              math.max(newestTime, time)
            }
                        
            loop(newestTime1)
          case _ => loop(newestTime)
        }
    }

    loop(Long.MinValue)
  }

  protected def loadFromSource(afterThisTime: Long): Long = {
    fromTime = afterThisTime + 1

    var loadedTime1 = loadedTime
    if (!connect) {
      return loadedTime1
    }
        
    try {
      request
      loadedTime1 = read
    } catch {
      case ex: Exception => ex.printStackTrace
    }

    loadedTime1
  }

  override def displayName: String = "Yahoo! Finance Internet"

  def defaultDateFormatPattern: String = "yyyy-MM-dd"

  def sourceSerialNumber = 1

  override def supportedFreqs: Array[TFreq] = {
    Array(TFreq.DAILY)
  }

  override def icon: Option[Image] = {
    val img = try {
      ImageIO.read(new File("org/aiotrade/lib/dataserver/yahoo/resources/favicon_yahoo.png"))
    } catch {case _ => null}

    if (img == null) None else Some(img)
  }

  override def sourceTimeZone: TimeZone = {
    TimeZone.getTimeZone("America/New_York")
  }
  
  def exchangeOf(symbol: String): Exchange = {
    YahooQuoteServer.exchangeOf(symbol)
  }

  def toSourceSymbol(exchange: Exchange, uniSymbol: String): String = {
    exchange.code match {
      case "NYSE" =>
        uniSymbol
      case "SHSE" =>
        uniSymbol + ".SS"
      case "SZSE" =>
        uniSymbol + ".SZ"
      case "LDSE" =>
        uniSymbol + ".L"
    }
  }
}



