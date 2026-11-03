package cl.duoc.sabaterreno

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import cl.duoc.sabaterreno.data.local.SabaDatabase
import cl.duoc.sabaterreno.data.local.SesionStore
import cl.duoc.sabaterreno.data.repo.SabaRepository
import cl.duoc.sabaterreno.ui.SabaApp
import cl.duoc.sabaterreno.ui.theme.SabaTerrenoTheme

/**
 * Hereda de FragmentActivity porque BiometricPrompt lo exige para poder
 * mostrar su diálogo sobre la pantalla actual.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repo = SabaRepository(SabaDatabase.obtener(applicationContext))
        val store = SesionStore(applicationContext)
        setContent {
            SabaTerrenoTheme {
                SabaApp(repo, store)
            }
        }
    }
}
