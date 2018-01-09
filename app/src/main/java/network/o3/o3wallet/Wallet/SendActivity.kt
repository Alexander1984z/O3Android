package network.o3.o3wallet.Wallet

import android.app.Activity
import android.app.Fragment
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import network.o3.o3wallet.API.NEO.NeoNodeRPC
import android.widget.*
import com.google.zxing.integration.android.IntentIntegrator
import network.o3.o3wallet.Account
import network.o3.o3wallet.PersistentStore
import network.o3.o3wallet.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton

class SendActivity : AppCompatActivity() {

    lateinit var addressTextView: TextView
    lateinit var amountTextView: TextView
    lateinit var selectedAsset: NeoNodeRPC.Asset
    lateinit var noteTextView: TextView
    lateinit var selectedAssetTextView: TextView
    lateinit var sendButton: Button
    lateinit var pasteAddressButton: Button
    lateinit var scanAddressButton: Button
    lateinit var view: View

    public val ARG_REVEAL_SETTINGS: String = "arg_reveal_settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        this.title = resources.getString(R.string.send)
        view = findViewById<View>(R.id.root_layout)
        addressTextView = findViewById<EditText>(R.id.addressTextView)
        amountTextView = findViewById<EditText>(R.id.amountTextView)
        noteTextView = findViewById<EditText>(R.id.noteTextView)
        sendButton = findViewById<Button>(R.id.sendButton)
        pasteAddressButton = findViewById<Button>(R.id.pasteAddressButton)
        scanAddressButton = findViewById<Button>(R.id.scanAddressButton)
        selectedAssetTextView = findViewById<TextView>(R.id.selectedAssetTextView)

        selectedAsset = NeoNodeRPC.Asset.NEO
        amountTextView.keyListener = DigitsKeyListener.getInstance("0123456789")
        selectedAssetTextView.text = selectedAsset.name.toUpperCase()
        addressTextView.afterTextChanged { checkEnableSendButton() }
        amountTextView.afterTextChanged { checkEnableSendButton() }
        selectedAssetTextView.setOnClickListener { toggleAsset() }

        sendButton.isEnabled = false
        sendButton.setOnClickListener { sendTapped() }

        pasteAddressButton.setOnClickListener { pasteAddressTapped() }
        scanAddressButton.setOnClickListener { scanAddressTapped() }

        val extras = intent.extras
        if (extras != null) {
            val address = extras.getString("address")
            addressTextView.text = address
            amountTextView.requestFocus()
            showFoundContact(address)
        }
    }

    fun showFoundContact(address:String) {
        val contacts = PersistentStore.getContacts()
        val foundContact = contacts.find { it.address == address }
        val toLabel = findViewById<TextView>(R.id.sendToLabel)
        if (foundContact != null) {
            toLabel.text = "To: %s".format(foundContact.nickname)
        } else {
            toLabel.text = "To"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item!!.itemId == R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private fun checkEnableSendButton() {
        showFoundContact(addressTextView.text.trim().toString())
        sendButton.isEnabled = (addressTextView.text.trim().count() > 0 && amountTextView.text.count() > 0)
    }

    private fun toggleAsset() {
        if (selectedAsset == NeoNodeRPC.Asset.NEO) {
            selectedAsset = NeoNodeRPC.Asset.GAS
            amountTextView.setRawInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED)
            amountTextView.keyListener = DigitsKeyListener.getInstance("0123456789.")
        } else {
            selectedAsset = NeoNodeRPC.Asset.NEO
            amountTextView.setRawInputType(InputType.TYPE_CLASS_NUMBER)
            amountTextView.keyListener = DigitsKeyListener.getInstance("0123456789")
            if (amountTextView.text.trim().count() > 0) {
                var amount = amountTextView.text.trim().toString().toDouble()
                amountTextView.text = Math.round(amount).toString()
            }
        }
        selectedAssetTextView.text = selectedAsset.name.toUpperCase()
    }

    private fun send() {
        //validate field
        val address = addressTextView.text.trim().toString()
        var amount = amountTextView.text.trim().toString().toDouble()

        if (amount == 0.0) {
            baseContext.toast(resources.getString(R.string.amount_must_be_nonzero))
            return
        }
        val wallet = Account.getWallet()
        val toast = baseContext.toastUntilCancel(resources.getString(R.string.sending_in_progress))
        sendButton.isEnabled = false
        NeoNodeRPC(PersistentStore.getNodeURL()).sendAssetTransaction(wallet!!, this.selectedAsset, amount, address, null) {
            runOnUiThread {
                toast.cancel()
                val error = it.second
                val success = it.first
                if (success == true) {
                    baseContext!!.toast(resources.getString(R.string.sent_successfully))
                    Handler().postDelayed(Runnable {
                        finish()
                    }, 1000)
                } else {
                    this.checkEnableSendButton()
                    val message = resources.getString(R.string.send_error)
                    val snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                    snack.setAction("Close") {
                        finish()
                    }
                    snack.show()
                }
            }
        }
    }

    private fun sendTapped() {
        this.hideKeyboard()
        //validate field
        val address = addressTextView.text.trim().toString()
        var amount = amountTextView.text.trim().toString().toDouble()

        if (amount == 0.0) {
            baseContext.toast(resources.getString(R.string.amount_must_be_nonzero))
            return
        }

        NeoNodeRPC(PersistentStore.getNodeURL()).validateAddress(address) {
            if (it.second != null || it?.first == false) {
                runOnUiThread {
                    alert(resources.getString(R.string.invalid_neo_address), resources.getString(R.string.error)) {
                        yesButton {
                            addressTextView.requestFocus()
                        }
                    }.show()
                }
            } else {
                runOnUiThread {
                    alert(resources.getString(R.string.send_confirmation, amount.toString(),
                            this.selectedAsset.name.toUpperCase(), address)) {
                        positiveButton(resources.getString(R.string.send)) {
                            send()
                        }
                        negativeButton(resources.getString(R.string.cancel)) {

                        }
                    }.show()
                }
            }
        }
    }

    private fun pasteAddressTapped() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null) {
            val item = clip.getItemAt(0)
            addressTextView.text = item.text.toString()
        }
    }


    fun scanAddressTapped() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
        integrator.setPrompt(resources.getString(R.string.scan_prompt_qr_send))
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, resources.getString(R.string.cancelled), Toast.LENGTH_LONG).show()
            } else {
                addressTextView.setText(result.contents)
            }
        }
    }
}

fun TextView.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.hideKeyboard() {
    activity.hideKeyboard(view)
}

fun Activity.hideKeyboard() {
    hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
}