package com.example.ejercicio2rrle

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.URL


class qr : AppCompatActivity(),ZXingScannerView. ResultHandler{
    private val PERMISO_CAMARA = 1
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checarPermiso()){
                //Se concedio el permiso

            }else{
                solicitarPermiso()
            }
        }

        scannerView?.setResultHandler(this)
        scannerView?.startCamera()

    }

    private fun solicitarPermiso() {
        ActivityCompat.requestPermissions(this@qr, arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
    }

    private fun checarPermiso(): Boolean {
        return (ContextCompat.checkSelfPermission(this@qr, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    override fun handleResult(p0: Result?) {
        //Codigo QR leido.
        val scanResult = p0?.text
        Log.d("QR_Leido", scanResult!!)

        print(scanResult)
        //Filtro para el url
        if(scanResult.startsWith(getString(R.string.URL))){
            val url = URL(scanResult)
            val i = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(scanResult))
            startActivity(i)
            finish()


//Filtro para la VCard
        }else if (scanResult.startsWith("BEGIN:VCARD")){

            //val tokens = scanResult.split("\n")




            /*val vc = adapter.getItem(position).getVcard().toString();
            Intent i = new Intent();
            i.setAction(android.content.Intent.ACTION_VIEW);
            i.setDataAndType(Uri.parse(adapter.getItem(position).getResult().toString()),"text/x-vcard");
            startActivity(i);

             */

            val vCObtenido : List<String> = scanResult.split("\n")
            val nombre: List<String> = vCObtenido[3].split("FN:")
            val org: List<String> = vCObtenido[4].split("ORG:")
            val title: List<String> = vCObtenido[5].split("TITLE:")
            val  adr: List<String> = vCObtenido[6].split("ADR:")
            val telWork: List<String> = vCObtenido[7].split("TEL;WORK;VOICE:")
            val telCel: List<String> = vCObtenido[8].split("TEL;CEL:")
            val email: List<String> = vCObtenido[10].split("EMAIL;WORK;INTERNET:")



            val vCardIntent = Intent(Intent.ACTION_INSERT_OR_EDIT)
            vCardIntent.setType(ContactsContract.Contacts.CONTENT_TYPE)
            vCardIntent.putExtra(ContactsContract.Intents.Insert.NAME,nombre[0])
            vCardIntent.putExtra(ContactsContract.Intents.Insert.COMPANY,org[0])
            vCardIntent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE,title[0])
            vCardIntent.putExtra(ContactsContract.Intents.Insert.PHONE,telCel[0] )
            vCardIntent.putExtra(ContactsContract.Intents.Insert.EMAIL,email[0])
            startActivity(vCardIntent)
            finish()



//Filtro para el correo
        }else if (scanResult.startsWith(getString(R.string.CORREO))){
            val mailObtenido: List<String> = scanResult.split(";")
            val mail1: List<String> = mailObtenido[0].split(getString(R.string.smain1))
            val mail2: List<String> = mailObtenido[1].split(getString(R.string.smail2))
            val mail3: List<String> = mailObtenido[2].split(getString(R.string.smail3))
            val mails = arrayOf(mail1[1])


            var intent = Intent(Intent.ACTION_SENDTO)
            intent.setType("*/*")
            intent.setData(Uri.parse("mailto:"))
            intent.putExtra(Intent.EXTRA_EMAIL,mails)
            intent.putExtra(Intent.EXTRA_SUBJECT,mail2[1])
            intent.putExtra(Intent.EXTRA_TEXT, mail3[1])
            startActivity(intent)
            finish()

//Filtro para el SMS
        }else if(scanResult.startsWith(getString(R.string.SMS))){
            val smsObtenido: List<String> = scanResult.split(":")


            var sendsms = Intent(Intent.ACTION_VIEW)
            sendsms.setData(Uri.parse("sms:${smsObtenido[1]}"))
            //sendsms.putExtra(Intent.EXTRA_PHONE_NUMBER,smsObtenido[1])
            sendsms.putExtra("sms_body",smsObtenido[2])
            startActivity(sendsms)
            finish()

//Filtro para los QR no validos
        }else{
            androidx.appcompat.app.AlertDialog.Builder(this@qr)
                .setTitle(getString(R.string.TituloE))
                .setMessage(getString(R.string.MensajeQRinvalido))
                .setPositiveButton(getString(R.string.Aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    finish()
                })
                .create()
                .show()
        }

    }

    override fun onResume() {
        super.onResume()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checarPermiso()){
                if(scannerView == null){
                    scannerView = ZXingScannerView(this)
                    setContentView(scannerView)
                }

                scannerView?.setResultHandler(this)
                scannerView?.startCamera()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView?.stopCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){

            PERMISO_CAMARA -> {
                if(grantResults.isNotEmpty()){
                    if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                                androidx.appcompat.app.AlertDialog.Builder(this@qr)
                                    .setTitle(getString(R.string.TituloPermi))
                                    .setMessage(getString(R.string.MensajePermisos))
                                    .setPositiveButton(getString(R.string.Aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                                        requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
                                    })
                                    .setNegativeButton(getString(R.string.Cancelar), DialogInterface.OnClickListener { dialogInterface, i ->
                                        dialogInterface.dismiss()
                                        finish()
                                    })
                                    .create()
                                    .show()
                            }else{
                                Toast.makeText(this@qr, getString(R.string.MensajeNOPermiso), Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    }
                }
            }

        }
    }


}