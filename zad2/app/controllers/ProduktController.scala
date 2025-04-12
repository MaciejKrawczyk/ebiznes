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


}
