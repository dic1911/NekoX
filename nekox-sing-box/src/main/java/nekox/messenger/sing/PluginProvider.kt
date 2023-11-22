package nekox.messenger.sing

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

class PluginProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val result = MatrixCursor(projection)
        result.newRow().add("path", "sing-box")
        return result
    }

    override fun getType(uri: Uri): String {
        return "application/x-elf"
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        check(mode == "r")
        var libraryPath = context?.applicationInfo?.nativeLibraryDir
        check(libraryPath != null && libraryPath != "")
        libraryPath += "/sing-box.so"
        return ParcelFileDescriptor.open(File(libraryPath), ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        throw UnsupportedOperationException()
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        throw UnsupportedOperationException()
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        throw UnsupportedOperationException()
    }
}