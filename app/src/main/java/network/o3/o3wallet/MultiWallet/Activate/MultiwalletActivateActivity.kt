package network.o3.o3wallet.MultiWallet.Activate

import android.content.res.Resources
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import kotlinx.android.synthetic.main.token_sale_root_activity.*
import network.o3.o3wallet.PersistentStore
import network.o3.o3wallet.R
import org.jetbrains.anko.find

class MultiwalletActivateActivity : AppCompatActivity() {

    var viewModel = ActivateMultiWalletViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.multiwallet_activate_activity)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.actionbar_layout)
        find<TextView>(R.id.mytext).text = resources.getString(R.string.MULTIWALLET_activate_multiwallet)
        find<ImageButton>(R.id.rightNavButton).visibility = View.GONE
    }

    val currentFragment: Fragment?
        get() = add_multiwallet_nav_host.childFragmentManager.findFragmentById(R.id.add_multiwallet_nav_host)

    override fun onBackPressed() {
        if (currentFragment is EncryptExistingKeySuccessFragment) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        if (PersistentStore.getTheme() == "Dark") {
            theme.applyStyle(R.style.AppTheme_Dark, true)
        } else {
            theme.applyStyle(R.style.AppTheme_White, true)
        }
        return theme
    }
}
