package cn.ktorfitx.server.sample.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
	authentication {
		jwt {
			realm = "your jwt realm"
			verifier(jwtVerifier)
			validate {
			
			}
		}
	}
}

private val jwtVerifier by lazy {
	JWT.require(Algorithm.HMAC256("your jwt secret")!!)
		.withAudience("your jwt audience")
		.withIssuer("your jwt issuer")
		.build()
}