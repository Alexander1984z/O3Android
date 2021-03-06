package network.o3.o3wallet.MultiWallet.Activate

import android.arch.lifecycle.ViewModel
import com.amplitude.api.Amplitude
import com.github.salomonbrys.kotson.jsonObject
import neoutils.Neoutils
import network.o3.o3wallet.Account
import network.o3.o3wallet.NEP6
import org.json.JSONObject


class ActivateMultiWalletViewModel: ViewModel() {
    var password: String = ""
    var encryptedKey: String = ""

    fun encryptKey(password: String) {
        this.password = password
        val nep2 = Neoutils.neP2Encrypt(Account.getWallet().wif, password)
        encryptedKey = nep2.encryptedKey

        val nep6 = NEP6.getFromFileSystem()
        nep6.addEncryptedKey(nep2.address, "My O3 Wallet", encryptedKey)
        nep6.writeToFileSystem()
        nep6.makeNewDefault(nep2.address, password)
        Account.deleteKeyFromDevice()
        Amplitude.getInstance().logEvent("multiwallet_activated")
    }
}