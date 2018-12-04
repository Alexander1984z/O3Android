package network.o3.o3wallet.Dapp


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import network.o3.o3wallet.Account
import network.o3.o3wallet.MultiWallet.ManageMultiWallet.ManageWalletsBottomSheet
import network.o3.o3wallet.NEP6
import network.o3.o3wallet.R
import network.o3.o3wallet.RoundedBottomSheetDialogFragment
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk15.coroutines.onClick
import org.jetbrains.anko.support.v4.onUiThread
import org.opengraph.OpenGraph
import java.lang.Exception


class DappConnectionRequestBottomSheet : RoundedBottomSheetDialogFragment() {
    lateinit var mView: View
    lateinit var logoView: ImageView
    lateinit var titleView: TextView

    lateinit var addressTextView: TextView
    lateinit var addressNameTextView: TextView

    lateinit var swapWalletContainer: ConstraintLayout

    lateinit var acceptConnectionButton: Button
    lateinit var rejectConnectionButton: Button

    var dappMessage: DappMessage? = null

    val needReloadExposedDappWallet = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            reloadDappWallet()
        }
    }

    fun registerReceivers() {
        LocalBroadcastManager.getInstance(this.context!!).registerReceiver(needReloadExposedDappWallet,
                IntentFilter("update-exposed-dapp-wallet"))
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this.context!!).unregisterReceiver(needReloadExposedDappWallet)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        mView = inflater.inflate(R.layout.dapp_connection_request_bottom_sheet, container, false)
        logoView = mView.find(R.id.openGraphLogoView)
        titleView = mView.find(R.id.openGraphTitleView)
        addressTextView = mView.find(R.id.walletAddressTextView)
        addressNameTextView = mView.find(R.id.walletNameTextView)
        swapWalletContainer = mView.find(R.id.swapWalletContainer)

        swapWalletContainer.onClick {
            val swapWalletSheet = DappWalletForSessionBottomSheet.newInstance()
            swapWalletSheet.show(activity!!.supportFragmentManager, swapWalletSheet.tag)
        }

        registerReceivers()
        loadOpenGraphDetails()
        setAccountDetails()
        setupConnectionResultButtons()
        return mView
    }

    fun setupConnectionResultButtons() {
        acceptConnectionButton = mView.find(R.id.acceptConnectionButton)
        rejectConnectionButton = mView.find(R.id.rejectConnectionButton)
        acceptConnectionButton.onClick {
            (activity as DAppBrowserActivityV2).jsInterface.authorizedAccountCredentials(dappMessage!!)
            dismiss()
        }

        rejectConnectionButton.onClick {
            (activity as DAppBrowserActivityV2).jsInterface.rejectedAccountCredentials(dappMessage!!)
            dismiss()
        }
    }

    fun reloadDappWallet() {
        setAccountDetails()
    }

    fun setAccountDetails() {
        addressTextView.text = (activity as DAppBrowserActivityV2).jsInterface.getDappExposedWallet().address
        addressNameTextView.text = (activity as DAppBrowserActivityV2).jsInterface.getDappExposedWalletName()
    }

    fun loadOpenGraphDetails() {
        val url = arguments!!.getString("url")
        try {
            bg {
                val dapp = OpenGraph(url, true)
                val title = dapp.getContent("title")
                val image = dapp.getContent("image")

                onUiThread {
                    titleView.text = title
                    Glide.with(mView).load(image).into(logoView)
                }
            }

        } catch (e: Exception) {

        }
    }

    companion object {
        fun newInstance(): DappConnectionRequestBottomSheet {
            return DappConnectionRequestBottomSheet()
        }
    }
}