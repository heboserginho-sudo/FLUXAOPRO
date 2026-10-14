package com.example.data.i18n

object NotificationTemplateService {

    enum class NotificationType {
        ORDER_CONFIRMED,
        INVOICE_ISSUED,
        PAYMENT_RECEIVED,
        BUDGET_LIMIT_WARNING,
        GOAL_REACHED,
        APPOINTMENT_REMINDER
    }

    fun getNotificationMessage(
        type: NotificationType,
        language: AppLanguage,
        vararg args: String
    ): Pair<String, String> {
        val title: String
        val body: String

        when (type) {
            NotificationType.ORDER_CONFIRMED -> {
                val orderId = args.getOrNull(0) ?: "#"
                when (language) {
                    AppLanguage.ENGLISH -> {
                        title = "Order Confirmed"
                        body = "Your order $orderId has been confirmed."
                    }
                    AppLanguage.FRENCH -> {
                        title = "Commande Confirmée"
                        body = "Votre commande $orderId a été confirmée."
                    }
                    AppLanguage.SPANISH -> {
                        title = "Pedido Confirmado"
                        body = "Su pedido $orderId ha sido confirmado."
                    }
                    else -> {
                        title = "Pedido Confirmado"
                        body = "O seu pedido $orderId foi confirmado com sucesso."
                    }
                }
            }
            NotificationType.INVOICE_ISSUED -> {
                val invoiceNumber = args.getOrNull(0) ?: ""
                val amount = args.getOrNull(1) ?: ""
                when (language) {
                    AppLanguage.ENGLISH -> {
                        title = "New Invoice Issued"
                        body = "Invoice $invoiceNumber ($amount) is now available for payment."
                    }
                    AppLanguage.FRENCH -> {
                        title = "Nouvelle Facture Émise"
                        body = "La facture $invoiceNumber ($amount) est disponible au paiement."
                    }
                    AppLanguage.SPANISH -> {
                        title = "Nueva Factura Emitida"
                        body = "La factura $invoiceNumber ($amount) ya está disponible."
                    }
                    else -> {
                        title = "Fatura Emitida"
                        body = "A fatura comercial $invoiceNumber ($amount) foi emitida com sucesso."
                    }
                }
            }
            NotificationType.PAYMENT_RECEIVED -> {
                val amount = args.getOrNull(0) ?: ""
                when (language) {
                    AppLanguage.ENGLISH -> {
                        title = "Payment Received"
                        body = "Payment of $amount has been recorded in your cash flow."
                    }
                    AppLanguage.FRENCH -> {
                        title = "Paiement Reçu"
                        body = "Le paiement de $amount a été enregistré dans votre trésorerie."
                    }
                    AppLanguage.SPANISH -> {
                        title = "Pago Recibido"
                        body = "El pago de $amount ha sido registrado en su flujo de caja."
                    }
                    else -> {
                        title = "Pagamento Recebido"
                        body = "O recebimento de $amount foi confirmado no seu fluxo de caixa."
                    }
                }
            }
            NotificationType.BUDGET_LIMIT_WARNING -> {
                val category = args.getOrNull(0) ?: ""
                val percent = args.getOrNull(1) ?: "80%"
                when (language) {
                    AppLanguage.ENGLISH -> {
                        title = "Budget Alert ($percent)"
                        body = "Expenses in $category have reached $percent of your planned budget."
                    }
                    AppLanguage.FRENCH -> {
                        title = "Alerte de Budget ($percent)"
                        body = "Les dépenses en $category ont atteint $percent de votre budget."
                    }
                    AppLanguage.SPANISH -> {
                        title = "Alerta de Presupuesto ($percent)"
                        body = "Los gastos en $category han alcanzado el $percent de su presupuesto."
                    }
                    else -> {
                        title = "Aviso de Orçamento ($percent)"
                        body = "As despesas na categoria $category atingiram $percent do limite estipulado."
                    }
                }
            }
            NotificationType.GOAL_REACHED -> {
                val goalTitle = args.getOrNull(0) ?: ""
                when (language) {
                    AppLanguage.ENGLISH -> {
                        title = "Goal Achieved!"
                        body = "Congratulations! You have reached your financial goal: $goalTitle."
                    }
                    AppLanguage.FRENCH -> {
                        title = "Objectif Atteint !"
                        body = "Félicitations ! Vous avez atteint votre objectif : $goalTitle."
                    }
                    AppLanguage.SPANISH -> {
                        title = "¡Meta Alcanzada!"
                        body = "¡Felicidades! Has completado tu meta financiera: $goalTitle."
                    }
                    else -> {
                        title = "Meta Concluída!"
                        body = "Parabéns! Concluiu com êxito a sua meta financeira: $goalTitle."
                    }
                }
            }
            NotificationType.APPOINTMENT_REMINDER -> {
                val client = args.getOrNull(0) ?: ""
                val time = args.getOrNull(1) ?: ""
                when (language) {
                    AppLanguage.ENGLISH -> {
                        title = "Appointment Reminder"
                        body = "Upcoming session with $client scheduled for $time."
                    }
                    AppLanguage.FRENCH -> {
                        title = "Rappel de Rendez-vous"
                        body = "Séance prévue avec $client à $time."
                    }
                    AppLanguage.SPANISH -> {
                        title = "Recordatorio de Cita"
                        body = "Próxima sesión con $client programada a las $time."
                    }
                    else -> {
                        title = "Lembrete de Agendamento"
                        body = "Tem um compromisso agendado com $client para as $time."
                    }
                }
            }
        }

        return Pair(title, body)
    }

    fun buildWhatsAppInvoiceMessage(
        language: AppLanguage,
        companyName: String,
        clientName: String,
        invoiceNumber: String,
        amountFormatted: String,
        iban: String
    ): String {
        return when (language) {
            AppLanguage.ENGLISH -> """
                Hello $clientName,
                This is $companyName. Please find your invoice $invoiceNumber for $amountFormatted.
                Payment details (IBAN / Account): $iban
                Thank you for your business!
            """.trimIndent()

            AppLanguage.FRENCH -> """
                Bonjour $clientName,
                De la part de $companyName. Voici votre facture $invoiceNumber d'un montant de $amountFormatted.
                Coordonnées de paiement (IBAN / Compte) : $iban
                Merci pour votre confiance !
            """.trimIndent()

            AppLanguage.SPANISH -> """
                Hola $clientName,
                Le escribe $companyName. Adjuntamos su factura $invoiceNumber por el importe de $amountFormatted.
                Datos para transferencia (IBAN / Cuenta): $iban
                ¡Gracias por su preferencia!
            """.trimIndent()

            else -> """
                Olá $clientName,
                Da parte de $companyName. Segue a fatura comercial $invoiceNumber no valor de $amountFormatted.
                Coordenadas bancárias para liquidação (IBAN): $iban
                Agradecemos a sua preferência!
            """.trimIndent()
        }
    }
}
