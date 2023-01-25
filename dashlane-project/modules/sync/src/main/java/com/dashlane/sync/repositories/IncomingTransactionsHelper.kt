package com.dashlane.sync.repositories

import com.dashlane.cryptography.CryptographyEngineFactory
import com.dashlane.server.api.endpoints.sync.SyncDownloadService
import com.dashlane.server.api.endpoints.sync.SyncDownloadTransaction
import com.dashlane.sync.domain.IncomingTransaction
import com.dashlane.sync.domain.TransactionCipher
import com.dashlane.sync.util.SyncLogs
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject



class IncomingTransactionsHelper @Inject constructor(
    private val transactionCipher: TransactionCipher,
    private val syncLogs: SyncLogs
) {
    suspend fun readTransactions(
        downloadData: SyncDownloadService.Data,
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncProgressChannel: SyncProgressChannel?,
        sharedIds: Set<String>
    ): Result {
        syncLogs.onDecipherTransactionsStart()

        val ignoredErrors = mutableListOf<Throwable>()

        
        val transactionErrors = mutableListOf<Throwable>()
        val incomingTransactions =
            decipherTransactions(
                downloadData.transactions,
                cryptographyEngineFactory,
                syncProgressChannel,
                transactionErrors,
                sharedIds
            )

        ignoredErrors.addAll(transactionErrors)

        syncLogs.onDecipherTransactionsDone(incomingTransactions.size, transactionErrors.size)

        return Result(
            incomingTransactions,
            ignoredErrors,
            SyncRepository.Result.Statistics.Incoming(
                incomingTransactions.count { it is IncomingTransaction.Update },
                incomingTransactions.count { it is IncomingTransaction.Delete }
            )
        )
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private suspend fun decipherTransactions(
        transactions: List<SyncDownloadTransaction>,
        cryptographyEngineFactory: CryptographyEngineFactory,
        syncProgressChannel: SyncProgressChannel?,
        ignoredErrors: MutableList<Throwable>,
        sharedIds: Set<String>
    ): List<IncomingTransaction> = coroutineScope {
        
        val transactionCount = transactions.size
        val progressActor = actor<Unit> {
            var index = 0
            consumeEach {
                index++
                syncProgressChannel?.trySend(SyncProgress.DecipherRemote(index, transactionCount))
            }
        }

        val (decipheredTransactions, errors) =
            transactionCipher.decipherIncomingTransactions(
                transactions,
                cryptographyEngineFactory,
                sharedIds,
                progressActor
            )

        progressActor.close()

        ignoredErrors += errors

        decipheredTransactions
    }

    data class Result(
        val transactions: List<IncomingTransaction>,
        val transactionErrors: List<Throwable>,
        val statistics: SyncRepository.Result.Statistics.Incoming
    )
}