package controllers

import javax.inject._
import play.api.mvc._
import scala.collection.mutable.ListBuffer
import play.api.libs.json._

@Singleton
class KoszykController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // Wzorując się na definicji Produktu z ProduktController
  case class Produkt(id: Long, nazwa: String)
  implicit val produktFormat: OFormat[Produkt] = Json.format[Produkt]

  // Definicja modelu Koszyk – przykładowo koszyk zawiera id oraz listę produktów
  case class Koszyk(id: Long, produkty: List[Produkt])
  implicit val koszykFormat: OFormat[Koszyk] = Json.format[Koszyk]

  // Przykładowa pamięć podręczna koszyków
  private val koszyki = ListBuffer[Koszyk]()

  // GET /koszyki - Lista wszystkich koszyków
  def listaKoszykow() = Action { implicit request: Request[AnyContent] =>
    Ok(Json.toJson(koszyki))
  }

  // GET /koszyki/:id - Szczegóły koszyka o podanym id
  def koszykSzczegoly(id: Long) = Action { implicit request: Request[AnyContent] =>
    koszyki.find(_.id == id) match {
      case Some(koszyk) => Ok(Json.toJson(koszyk))
      case None => NotFound(Json.obj("error" -> s"Koszyk o id $id nie został znaleziony"))
    }
  }

  // POST /koszyki - Dodanie nowego koszyka
  def dodajKoszyk() = Action(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Koszyk].fold(
      errors => BadRequest(Json.obj("error" -> "Nieprawidłowy format JSON")),
      koszyk => {
        koszyki += koszyk
        Created(Json.toJson(koszyk))
      }
    )
  }

  // PUT /koszyki/:id - Aktualizacja istniejącego koszyka
  def edytujKoszyk(id: Long) = Action(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Koszyk].fold(
      errors => BadRequest(Json.obj("error" -> "Nieprawidłowy format JSON")),
      updatedKoszyk => {
        koszyki.indexWhere(_.id == id) match {
          case -1 => NotFound(Json.obj("error" -> s"Koszyk o id $id nie istnieje"))
          case idx =>
            koszyki.update(idx, updatedKoszyk.copy(id = id))
            Ok(Json.toJson(updatedKoszyk.copy(id = id)))
        }
      }
    )
  }

  // DELETE /koszyki/:id - Usunięcie koszyka
  def usunKoszyk(id: Long) = Action { implicit request: Request[AnyContent] =>
    koszyki.indexWhere(_.id == id) match {
      case -1 => NotFound(Json.obj("error" -> s"Koszyk o id $id nie istnieje"))
      case idx =>
        koszyki.remove(idx)
        Ok(Json.obj("message" -> s"Koszyk o id $id został usunięty"))
    }
  }
}
