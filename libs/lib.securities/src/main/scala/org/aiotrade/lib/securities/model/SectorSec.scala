package org.aiotrade.lib.securities.model

import ru.circumflex.orm.Table

class SectorSec {
  var sector: Sector = _
  var sec: Sec = _
  
  var weight: Float = _
  var validFrom: Long = _
  var validTo: Long = _
}

object SectorSecs extends Table[SectorSec] {
  val sector  = "sectors_id" BIGINT() REFERENCES(Sectors)
  val sec = "secs_id" BIGINT() REFERENCES(Secs)
  
  val weight = "weight" FLOAT()
  val validFrom = "validFrom" BIGINT()
  val validTo = "validTo" BIGINT()
  
  val sectorIdx = getClass.getSimpleName + "_sector_idx" INDEX(sector.name)
  val secIdx = getClass.getSimpleName + "_sec_idx" INDEX(sec.name)
  val validFromIdx = getClass.getSimpleName + "_validFrom_idx" INDEX(validFrom.name)
  val validToIdx = getClass.getSimpleName + "_validToFrom_idx" INDEX(validTo.name)
}
