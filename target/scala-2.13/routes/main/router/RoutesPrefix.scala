// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/sarina.salamon/Documents/play-template/conf/routes
// @DATE:Fri Jun 24 15:06:46 BST 2022


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
