package argonaut

import scalaz._, Scalaz._

sealed trait CodecJson[A] extends EncodeJson[A] with DecodeJson[A] {
  val Encoder: EncodeJson[A]
  val Decoder: DecodeJson[A]

  override def encode(a: A) = Encoder.encode(a)
  override def decode(c: HCursor) = Decoder.decode(c)
  override def tryDecode(c: ACursor) = Decoder.tryDecode(c)

  trait CodecLaw {
    def encodedecode(a: A)(implicit A: Equal[A]) =
      decodeJson(encode(a)).value.exists (_ === a)
  }

  def codecLaw = new CodecLaw {}
}

object CodecJson extends CodecJsons {
  def apply[A](encoder: A => Json, decoder: HCursor => DecodeResult[A]): CodecJson[A] =
    derived(EncodeJson(encoder), DecodeJson(decoder))

  def withReattempt[A](encoder: A => Json, decoder: ACursor => DecodeResult[A]): CodecJson[A] =
    derived(EncodeJson(encoder), DecodeJson.withReattempt(decoder))

  def derived[A](implicit E: EncodeJson[A], D: DecodeJson[A]) =
    new CodecJson[A] {
      val Encoder = E
      val Decoder = D
    }
}

trait CodecJsons extends GeneratedCodecJsons
