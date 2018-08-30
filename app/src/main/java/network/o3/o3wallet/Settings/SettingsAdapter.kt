package network.o3.o3wallet.Settings

import android.app.KeyguardManager
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri
import network.o3.o3wallet.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import android.os.Build
import kotlinx.android.synthetic.main.settings_activity_add_contact.view.*
import network.o3.o3wallet.Onboarding.LandingActivity
import java.util.*
import org.jetbrains.anko.image


/**
 * Created by drei on 12/8/17.
 */

class SettingsAdapter(context: Context, fragment: SettingsFragment): BaseAdapter() {
    private val mContext: Context
    private var mFragment: SettingsFragment
    var settingsTitles = context.resources.getStringArray(R.array.SETTINGS_settings_menu_titles)
    var images =  listOf(R.drawable.ic_lock_alt, R.drawable.ic_currency, R.drawable.ic_currency,
            R.drawable.ic_settingswatchonlyaddressicon,
            R.drawable.ic_comment, R.drawable.ic_settingscontacticon,
            R.drawable.ic_settings_logout, R.drawable.ic_mobile_android_alt, R.drawable.ic_bug)
    init {
        mContext = context
        mFragment = fragment
    }

    enum class CellType {
        PRIVATEKEY, CURRENCY, THEME,
        WATCHADRESS, SUPPORT, CONTACT, LOGOUT,
        VERSION, ADVANCED

    }

    override fun getItem(position: Int): Pair<String, Int> {
        return Pair(settingsTitles[position], images[position])
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        if (BuildConfig.DEBUG) {
            return settingsTitles.count()
        }
        return settingsTitles.count() - 1
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(mContext)
        val view = layoutInflater.inflate(R.layout.settings_row_layout, viewGroup, false)
        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        titleTextView.text = getItem(position).first
        if (position == CellType.VERSION.ordinal) {
            val version = mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionName
            titleTextView.text = mContext.resources.getString(R.string.SETTINGS_version, version)
        }

        view.findViewById<ImageView>(R.id.settingsIcon).image = mContext.getDrawable(images[position])

        view.setOnClickListener {
            getClickListenerForPosition(position)
        }
        return view
    }

    fun getClickListenerForPosition(position: Int) {
        if (position == CellType.CURRENCY.ordinal  ) {
            val currencyModal = CurrencyFragment.newInstance()
            currencyModal.show((mContext as AppCompatActivity).supportFragmentManager, currencyModal.tag)
            return
        } else if(position == CellType.THEME.ordinal) {
            val themeModal = ThemeModalFragment.newInstance()
            themeModal.show((mContext as AppCompatActivity).supportFragmentManager, themeModal.tag)
            return
        } else if (position == CellType.WATCHADRESS.ordinal) {
            val watchAddressModal = WatchAddressFragment.newInstance()
            watchAddressModal.show((mContext as AppCompatActivity).supportFragmentManager, watchAddressModal.tag)
            return
        } else if (position == CellType.SUPPORT.ordinal) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://community.o3.network/"))
            startActivity(mContext, browserIntent, null)
        } else if (position == CellType.CONTACT.ordinal) {
            val intent = Intent(Intent.ACTION_VIEW)
            val data = Uri.parse("mailto:support@o3.network")
            intent.data = data
            startActivity(mContext, intent, null)
            return
        } else if (position == CellType.LOGOUT.ordinal) {
            mContext.alert(O3Wallet.appContext!!.resources.getString(R.string.SETTINGS_logout_warning)) {
                yesButton {
                    Account.deleteKeyFromDevice()
                    mFragment.activity?.finish()
                    val intent = Intent(mContext, LandingActivity::class.java)
                    startActivity(mContext, intent, null)
                }
                noButton {

                }
            }.show()

        } else if (position == CellType.PRIVATEKEY.ordinal) {
            val mKeyguardManager =  mContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (!mKeyguardManager.isKeyguardSecure) {
                // Show a message that the user hasn't set up a lock screen.
                Toast.makeText(mContext,
                        O3Wallet.appContext!!.resources.getString(R.string.ALERT_no_passcode_setup),
                        Toast.LENGTH_LONG).show()
                return
            } else {
                val intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null)
                if (intent != null) {
                    mFragment.startActivityForResult( intent, 0, null)
                }
            }
        } else if (position == CellType.ADVANCED.ordinal) {
            val intent = Intent(mContext, AdvancedSettingsActivity::class.java)
            mFragment.startActivity(intent)
        }
    }
}