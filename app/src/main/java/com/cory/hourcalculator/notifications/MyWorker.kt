package com.cory.hourcalculator.notifications

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorker(appContext: Context, workerParameters: WorkerParameters) : Worker(appContext, workerParameters) {

    override fun doWork(): Result {
        Log.d("Worker", "Perform long running task in scheduled job")

        return Result.success()
    }
}