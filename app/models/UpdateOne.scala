package models


import play.api.libs.json.{Json, OFormat}

  case class UpdateOneThing(nameOfDbColumn: String, value: String)

object UpdateOneThing {
  implicit val formats: OFormat[UpdateOneThing] = Json.format[UpdateOneThing] //writes turns datamodel to json?
}


