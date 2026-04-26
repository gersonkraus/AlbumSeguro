package com.familiaaco.ui.screens

import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.core.graphics.set
import com.familiaaco.data.models.MidiaDTO
import com.familiaaco.ui.utils.formatarDataParaExibicao
import com.familiaaco.viewmodel.ChildrenViewModel
import com.familiaaco.viewmodel.MediaViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.text.SimpleDateFormat
import java.util.*

private fun gerarQRCodeBitmap(conteudo: String, tamanho: Int = 512): Bitmap? {
    return try {
        val writer = MultiFormatWriter()
        val bitMatrix: BitMatrix = writer.encode(conteudo, BarcodeFormat.QR_CODE, tamanho, tamanho)
        val bitmap = Bitmap.createBitmap(tamanho, tamanho, Bitmap.Config.ARGB_8888)
        for (x in 0 until tamanho) {
            for (y in 0 until tamanho) {
                bitmap[x, y] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        bitmap
    } catch (_: Exception) {
        null
    }
}

private fun compartilharLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Acesse meu álbum no Álbum Seguro: $url")
        putExtra(Intent.EXTRA_SUBJECT, "Álbum Seguro")
    }
    val chooser = Intent.createChooser(intent, "Compartilhar link")
    context.startActivity(chooser)
}

private fun isVideoMidia(midia: MidiaDTO): Boolean {
    if (midia.tipo.equals("video", ignoreCase = true)) return true
    val url = midia.url.lowercase(Locale.ROOT)
    return url.contains(".mp4") || url.contains(".mov") || url.contains(".avi") ||
            url.contains(".mkv") || url.contains(".webm") || url.contains(".m4v")
}

private fun downloadMidia(context: Context, url: String, id: String) {
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("AlbumSeguro")
        .setDescription("Salvando imagem...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "AlbumSeguro/$id.jpg")
        .setAllowedOverMetered(true)
    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    dm.enqueue(request)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChildDetailScreen(navController: NavController, childId: String?, nomeInicial: String? = null) {
    if (childId == null) { LaunchedEffect(Unit) { navController.popBackStack() }; return }
    val context = LocalContext.current
    val childrenVm: ChildrenViewModel = viewModel(key = "children_$childId",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = ChildrenViewModel(context) as T
        })
    val mediaVm: MediaViewModel = viewModel(key = "media_$childId",
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = MediaViewModel(context) as T
        })
    val tokenState by childrenVm.tokenState.collectAsState()
    val mediaState by mediaVm.mediaState.collectAsState()
    val criancaAtual by childrenVm.criancaAtual.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var fotoPerfil by remember { mutableStateOf<String?>(null) }

    val fotoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val ext = context.contentResolver.getType(it)?.substringAfterLast('/') ?: "jpg"
            val mime = context.contentResolver.getType(it) ?: "image/jpeg"
            val stream = context.contentResolver.openInputStream(it) ?: return@let
            val bytes = stream.readBytes(); stream.close()
            val tmpFile = java.io.File(context.cacheDir, "foto_perfil.$ext")
            tmpFile.writeBytes(bytes)
            childrenVm.uploadFotoPerfil(childId, tmpFile, mime) { url -> fotoPerfil = url }
        }
    }

    var filtroTipo by remember { mutableStateOf("todos") }
    var filtroOrdem by remember { mutableStateOf("desc") }
    var showOrdemMenu by remember { mutableStateOf(false) }
    var midiaParaExcluir by remember { mutableStateOf<com.familiaaco.data.models.MidiaDTO?>(null) }
    var midiaVisualizada by remember { mutableStateOf<com.familiaaco.data.models.MidiaDTO?>(null) }
    var midiaOpcoes by remember { mutableStateOf<com.familiaaco.data.models.MidiaDTO?>(null) }
    var midiaEditDesc by remember { mutableStateOf<com.familiaaco.data.models.MidiaDTO?>(null) }
    var novaDescricao by remember { mutableStateOf("") }
    var showTokenDialog by remember { mutableStateOf(false) }
    var diasToken by remember { mutableStateOf("30") }
    var showQrDialog by remember { mutableStateOf(false) }
    var qrUrl by remember { mutableStateOf<String?>(null) }
    var showDeleteChildDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editNome by remember { mutableStateOf("") }
    var editData by remember { mutableStateOf("") }
    var editDescricao by remember { mutableStateOf("") }

    LaunchedEffect(childId) {
        childrenVm.carregarCrianca(childId)
    }

    LaunchedEffect(criancaAtual) {
        criancaAtual?.let { c ->
            c.fotoPerfil?.let { if (it.isNotBlank() && fotoPerfil == null) fotoPerfil = it }
        }
    }

    LaunchedEffect(childId, filtroTipo, filtroOrdem) {
        mediaVm.listarMidia(childId, filtroTipo.takeIf { it != "todos" }, filtroOrdem)
    }

    // Visualizador de mídia
    if (midiaVisualizada != null) {
        val m = midiaVisualizada!!
        AlertDialog(
            onDismissRequest = { midiaVisualizada = null },
            title = null,
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = m.url,
                        contentDescription = m.descricao,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                    if (!m.descricao.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(m.descricao, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(formatarDataParaExibicao(m.dataMomento), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { midiaVisualizada = null }) { Text("Fechar") }
            }
        )
    }

    // Menu de opções (long-press)
    if (midiaOpcoes != null) {
        val m = midiaOpcoes!!
        AlertDialog(
            onDismissRequest = { midiaOpcoes = null },
            title = { Text("Opções") },
            text = {
                Column {
                    TextButton(onClick = { midiaOpcoes = null; novaDescricao = m.descricao ?: ""; midiaEditDesc = m }, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Editar descrição")
                        }
                    }
                    TextButton(onClick = {
                        midiaOpcoes = null
                        downloadMidia(context, m.url, m._id)
                    }, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Salvar na galeria")
                        }
                    }
                    TextButton(onClick = { midiaOpcoes = null; midiaParaExcluir = m }, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text("Excluir", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { midiaOpcoes = null }) { Text("Cancelar") }
            }
        )
    }

    // Editar descrição da mídia
    if (midiaEditDesc != null) {
        AlertDialog(
            onDismissRequest = { midiaEditDesc = null },
            title = { Text("Editar descrição") },
            text = {
                OutlinedTextField(
                    value = novaDescricao,
                    onValueChange = { novaDescricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    mediaVm.editarMidia(midiaEditDesc!!._id, novaDescricao, childId)
                    midiaEditDesc = null
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { midiaEditDesc = null }) { Text("Cancelar") }
            }
        )
    }

    // Confirmar exclusão de mídia
    if (midiaParaExcluir != null) {
        AlertDialog(
            onDismissRequest = { midiaParaExcluir = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Excluir mídia?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mediaVm.deletarMidia(midiaParaExcluir!!._id, childId)
                        midiaParaExcluir = null
                    }
                ) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { midiaParaExcluir = null }) { Text("Cancelar") }
            }
        )
    }

    if (showTokenDialog) {
        AlertDialog(
            onDismissRequest = { showTokenDialog = false },
            icon = { Icon(Icons.Default.VpnKey, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Novo Token de Acesso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Defina por quantos dias o link será válido:", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("7", "15", "30", "90").forEach { dias ->
                            FilterChip(
                                selected = diasToken == dias,
                                onClick = { diasToken = dias },
                                label = { Text("${dias}d") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = diasToken,
                        onValueChange = { if (it.all { c -> c.isDigit() }) diasToken = it },
                        label = { Text("Dias") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showTokenDialog = false
                    childrenVm.gerarToken(childId, diasToken.toIntOrNull() ?: 30)
                }, enabled = diasToken.isNotBlank() && (diasToken.toIntOrNull() ?: 0) > 0
                ) { Text("Gerar") }
            },
            dismissButton = {
                TextButton(onClick = { showTokenDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteChildDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteChildDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Excluir criança?") },
            text = { Text("Todos os dados e mídias desta criança serão removidos. Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteChildDialog = false
                    childrenVm.deletarCrianca(childId) { navController.popBackStack() }
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteChildDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo de QR Code
    if (showQrDialog && !qrUrl.isNullOrBlank()) {
        val bitmap = remember(qrUrl) { gerarQRCodeBitmap(qrUrl!!, 512) }
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            icon = { Icon(Icons.Default.QrCode, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("QR Code do Álbum") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (bitmap != null) {
                        androidx.compose.foundation.Image(
                            painter = BitmapPainter(bitmap.asImageBitmap()),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(256.dp)
                        )
                    } else {
                        Text("Erro ao gerar QR Code")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Escaneie com o app ou compartilhe o link abaixo:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = qrUrl!!,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) { Text("Fechar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    compartilharLink(context, qrUrl!!)
                }) { Text("Compartilhar") }
            }
        )
    }

    if (showEditDialog) {
        var editDataExibicao by remember(editData) { mutableStateOf(formatarDataParaExibicao(editData)) }
        val calEdit = Calendar.getInstance()
        val editDatePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                val cal = Calendar.getInstance().apply { set(year, month, day) }
                editData = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                editDataExibicao = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
            },
            calEdit.get(Calendar.YEAR),
            calEdit.get(Calendar.MONTH),
            calEdit.get(Calendar.DAY_OF_MONTH)
        )
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar criança") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editNome, onValueChange = { editNome = it },
                        label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = editDataExibicao,
                        onValueChange = { },
                        label = { Text("Data de nascimento") },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { editDatePicker.show() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Selecionar data")
                            }
                        }
                    )
                    OutlinedTextField(value = editDescricao, onValueChange = { editDescricao = it },
                        label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    childrenVm.editarCrianca(childId, editNome, editData, editDescricao.ifEmpty { null })
                }, enabled = editNome.isNotBlank() && editData.isNotBlank()
                ) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(criancaAtual?.nome ?: nomeInicial ?: "Detalhes", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        criancaAtual?.let { c ->
                            editNome = c.nome
                            editData = c.dataNascimento ?: ""
                            editDescricao = c.descricao ?: ""
                        }
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteChildDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .combinedClickable(onClick = { fotoLauncher.launch("image/*") })
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!fotoPerfil.isNullOrBlank()) {
                        AsyncImage(model = fotoPerfil, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Foto de perfil", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    }
                }
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Token de Acesso", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(12.dp))
                    when (tokenState) {
                        is ChildrenViewModel.TokenState.Success -> {
                            val tokenSuccess = tokenState as ChildrenViewModel.TokenState.Success
                            val token = tokenSuccess.token
                            val childAlbumUrl = tokenSuccess.childAlbumUrl
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = token,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(token))
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copiar token",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            // Se tiver URL completa do álbum, mostra opções de QR, copiar link e compartilhar
                            if (!childAlbumUrl.isNullOrBlank()) {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = childAlbumUrl,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            qrUrl = childAlbumUrl
                                            showQrDialog = true
                                        },
                                        shape = RoundedCornerShape(50.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.QrCode, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Ver QR", style = MaterialTheme.typography.labelLarge)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(childAlbumUrl))
                                        },
                                        shape = RoundedCornerShape(50.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Copiar", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { compartilharLink(context, childAlbumUrl) },
                                    shape = RoundedCornerShape(50.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Compartilhar link", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                        is ChildrenViewModel.TokenState.Loading ->
                            CircularProgressIndicator(Modifier.size(24.dp))
                        is ChildrenViewModel.TokenState.Error ->
                            Text(
                                (tokenState as ChildrenViewModel.TokenState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        else ->
                            Text("Token não gerado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showTokenDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Novo Token", style = MaterialTheme.typography.labelLarge)
                        }
                        Button(
                            onClick = { navController.navigate("media_upload/$childId") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mídia", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("todos" to "Todos", "foto" to "Fotos", "video" to "Vídeos").forEach { (valor, label) ->
                    FilterChip(
                        selected = filtroTipo == valor,
                        onClick = { filtroTipo = valor },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                Spacer(Modifier.weight(1f))
                Box {
                    IconButton(onClick = { showOrdemMenu = true }) {
                        Icon(Icons.Default.Sort, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showOrdemMenu, onDismissRequest = { showOrdemMenu = false }) {
                        listOf("desc" to "Mais recentes", "asc" to "Mais antigas", "tamanho" to "Maior tamanho").forEach { (valor, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { filtroOrdem = valor; showOrdemMenu = false },
                                leadingIcon = { if (filtroOrdem == valor) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }

            when (mediaState) {
                is MediaViewModel.MediaState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is MediaViewModel.MediaState.Success -> {
                    val midias = (mediaState as MediaViewModel.MediaState.Success).midias
                    if (midias.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outlineVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Nenhuma mídia cadastrada",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(midias) { midia ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .combinedClickable(
                                            onClick = {
                                            if (isVideoMidia(midia)) {
                                                val encoded = android.util.Base64.encodeToString(midia.url.toByteArray(Charsets.UTF_8), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                                                navController.navigate("video_player/$encoded")
                                            } else {
                                                midiaVisualizada = midia
                                            }
                                        },
                                            onLongClick = { midiaOpcoes = midia }
                                        )
                                ) {
                                    val isVideo = isVideoMidia(midia)
                                    if (isVideo && midia.thumbnailUrl.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(androidx.compose.ui.graphics.Color.Black)
                                        )
                                    } else {
                                        AsyncImage(
                                            model = midia.thumbnailUrl ?: midia.url,
                                            contentDescription = midia.descricao,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                    if (isVideo) {
                                        Icon(
                                            Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.size(36.dp).align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is MediaViewModel.MediaState.Error ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            (mediaState as MediaViewModel.MediaState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                else -> {}
            }
        }
    }
}
