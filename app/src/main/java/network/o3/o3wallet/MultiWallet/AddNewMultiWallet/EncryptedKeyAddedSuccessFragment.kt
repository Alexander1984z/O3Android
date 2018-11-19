package network.o3.o3wallet.MultiWallet.AddNewMultiWallet

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import network.o3.o3wallet.R
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk15.coroutines.onClick

class EncryptedKeyAddedSuccessFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.multiwallet_watch_address_added, container, false)
        view.find<Button>(R.id.doneButton).onClick {
            activity?.finish()
        }
        return view
    }
}