export function validarArchivoImportacion(archivo) {
  if (!archivo) {
    return 'Selecciona un archivo para continuar.'
  }

  if (archivo.size === 0) {
    return 'El archivo está vacío.'
  }

  const extension = archivo.name.split('.').pop()?.toLowerCase()
  if (!['txt', 'xlsx'].includes(extension)) {
    return 'El formato no es compatible. Selecciona un archivo TXT o XLSX.'
  }

  return null
}

export function formatearTamanoArchivo(bytes) {
  if (bytes < 1024) {
    return `${bytes} B`
  }

  if (bytes < 1024 * 1024) {
    return `${Number((bytes / 1024).toFixed(1))} KB`
  }

  return `${Number((bytes / (1024 * 1024)).toFixed(1))} MB`
}
