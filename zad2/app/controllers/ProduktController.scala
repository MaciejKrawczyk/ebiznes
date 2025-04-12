package controllers

import javax.inject._
import play.api.mvc._
import scala.collection.mutable.ListBuffer
import play.api.libs.json._

@Singleton
class ProduktController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  case class Produkt(id: Long, nazwa: String)
  implicit val produktFormat: OFormat[Produkt] = Json.format[Produkt]

  private val produkty = ListBuffer(
    Produkt(1, "Laptop"),
    Produkt(2, "Telefon"),
    Produkt(3, "Tablet")
  )

  // GET /produkty - List all products
  def listaProduktow() = Action { implicit request: Request[AnyContent] =>
    Ok(Json.toJson(produkty))
  }

  // GET /produkty/:id - Get product details by id
  def produktSzczegoly(id: Long) = Action { implicit request: Request[AnyContent] =>
    produkty.find(_.id == id) match {
      case Some(produkt) => Ok(Json.toJson(produkt))
      case None => NotFound(Json.obj("error" -> s"Produkt o id $id nie został znaleziony"))
    }
  }

  // POST /produkty - Add a new product
  def dodajProdukt() = Action(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Produkt].fold(
      errors => BadRequest(Json.obj("error" -> "Nieprawidłowy format JSON")),
      produkt => {
        produkty += produkt
        Created(Json.toJson(produkt))
      }
    )
  }

  // PUT /produkty/:id - Update an existing product
  def edytujProdukt(id: Long) = Action(parse.json) { implicit request: Request[JsValue] =>
    request.body.validate[Produkt].fold(
      errors => BadRequest(Json.obj("error" -> "Nieprawidłowy format JSON")),
      updatedProdukt => {
        produkty.indexWhere(_.id == id) match {
          case -1 => NotFound(Json.obj("error" -> s"Produkt o id $id nie istnieje"))
          case idx =>
            produkty.update(idx, updatedProdukt.copy(id = id))
            Ok(Json.toJson(updatedProdukt.copy(id = id)))
        }
      }
    )
  }

  // DELETE /produkty/:id - Delete a product
  def usunProdukt(id: Long) = Action { implicit request: Request[AnyContent] =>
    produkty.indexWhere(_.id == id) match {
      case -1 => NotFound(Json.obj("error" -> s"Produkt o id $id nie istnieje"))
      case idx =>
        produkty.remove(idx)
        Ok(Json.obj("message" -> s"Produkt o id $id został usunięty"))
    }
  }
}
