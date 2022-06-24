package models


import play.api.libs.json.{Json, OFormat}

  case class UpdateOneName(id: String, name: String)

object UpdateOneName {
  implicit val formats: OFormat[UpdateOneName] = Json.format[UpdateOneName] //writes turns datamodel to json?
}


