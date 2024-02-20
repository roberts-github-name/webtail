//package webtail.springdemo
//
//import org.springframework.boot.{CommandLineRunner, SpringApplication}
//import org.springframework.boot.autoconfigure.SpringBootApplication
//
///**
// * Scala: class can't have a static main method, b/c no static keyword exists
// *        but if we put make it an object, the JVM code generated from object
// *        implements an object as a final class with a singleton instance,
// *        and Spring throws an exception about "can't be a final class" or w/e
// *
// * Solution: this stuff I found from a minimum working example.
// *
// * Impressions of Spring so far: pretty neat... but a LOT of very oddly specific
// * things you need, and no apparent organization or clear "if you see this error,
// * then try this."
// *
// * It's all just "well slap this esoteric annotation on something, and X doesn't work
// * because Y in another class doesn't have a @Bean annotation... because screw you I guess?"
// */
//
//@SpringBootApplication // shorthand for scanning for @Bean and @Configuration and some other stuff
//class WebsocketApp extends CommandLineRunner {
//  override def run(args: String*): Unit = {
//    println("Hello World!")
//  }
//}
//
//object WebsocketApp {
//  def main(args: Array[String]): Unit = {
//    /**
//     * Recommended structure:
//     *   have a static main the runs something annotated with @SpringBootApplication
//     *   or some combination of its underlying annotations and custom configs (@SBA is
//     *   shorthand for 5 or so annotations)
//     *
//     *   one of those annotations that @SBA does for you is an "autoscan" that looks for
//     *   all @Bean and @Configuration classes, and... does something with them. Reflexive
//     *   instantiation and run probably
//     *
//     *   it's further recommended that there be only one class with the @Configuration
//     *   annotation that handles all the, well, configuration
//     */
//    SpringApplication.run(classOf[WebsocketApp])
//  }
//}