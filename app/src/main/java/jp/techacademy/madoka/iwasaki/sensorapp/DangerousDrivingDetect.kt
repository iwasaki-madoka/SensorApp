package jp.techacademy.madoka.iwasaki.sensorapp

import android.content.Context
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import java.io.File
import java.io.IOException
import java.util.*

class DangerousDrivingDetect(sensorManager: SensorManager) : SensorEventListener{

    val FILENAME = "practicefile.txt"                   // 危険運転状況を記録するファイル名
    val WAIT_TIME = 2000                                // 一度危険運転を検知してから次に検知可能となるまでの待ち時間[ミリ秒]

    private var suddenMoveTime:Long = 0                 // 急停止・急加速時の時間を保持する変数
    private var sensorManager: SensorManager? = null    // センサーマネージャー


    init { // プライマリコンストラクタ（プロパティとコンストラクタの宣言をまとめて行う）

        //sensorManegerのインスタンスを取得
        this.sensorManager = sensorManager

        // Listenerの登録
        val accel = sensorManager!!.getDefaultSensor(
            Sensor.TYPE_LINEAR_ACCELERATION // 線形加速度計を使用する
            // Sensor.TYPE_ACCELEROMETER // 加速度(生データ)を使用する
        )
        sensorManager!!.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)

        // ファイルがすでに存在していた場合消去する
        val readFile = File(applicationContext.filesDir, FILENAME)
        if(readFile.exists()){ // すでにファイルが存在していたら
            deleteFile(FILENAME) // ファイルを消去
            Log.d("gyro_test","delete")
        }

    }

/*
    override fun onPause() {

        super.onPause()
        // Listenerを解除
        sensorManager!!.unregisterListener(this)
    }

 */

    // センサーの値が変化した際に呼び出されるイベント処理
    override fun onSensorChanged(event: SensorEvent) {
        val sensorX: Float
        val sensorY: Float
        val sensorZ: Float
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0]
            sensorY = event.values[1]
            sensorZ = event.values[2]


                if(getNowTime() > suddenMoveTime + WAIT_TIME ){ // 前回記録時からWAIT_TIMEミリ秒以上経過していたら

                    try { // ファイル書き込み時にI/OExceptionが発生するためのエラー処理

                        // ファイルをオープン
                        val fos = openFileOutput(FILENAME, Context.MODE_APPEND)

                        if(sensorZ >= 20) { // スマホの画面側に加速（急加速）
                            suddenMoveTime = getNowTime() // 記録時間を更新
                            Log.d(
                                "gyro_test", "Time: " + suddenMoveTime
                                        + "\tsensorZ: " + sensorZ + " accele"
                            )
                            fos.write("${suddenMoveTime} ${sensorZ} acsele\n".toByteArray())

                        } else if (sensorZ <= -20){ // スマホの背面側に加速（急停止）
                            suddenMoveTime = getNowTime() // 記録時間を更新
                            Log.d(
                                "gyro_test", "Time: " + suddenMoveTime
                                        + "\tsensorZ: " + sensorZ + " brake"
                            )
                            fos.write("${suddenMoveTime} ${sensorZ} brake\n".toByteArray())

                        }
                        fos.close() // ファイルをクローズ
                    }catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }

    // 1970 年 1 月 1 日 00:00:00から現在までの経過時間をミリ秒で表した数値を返す関数
    private fun getNowTime(): Long{
        val date = Date()
        return date.time
    }







}