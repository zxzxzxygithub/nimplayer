package com.netease.neliveplayer.demo.util.storage;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * package
 */
public class ExternalStorage {

    private static final String TAG = "NEExternalStorage";

    /**
     * 外部存储根目录
     */
    private Context context;

    private boolean hasPermission = true; // 是否拥有存储卡权限

    private String sdkStorageRoot = null;

    private static ExternalStorage instance;

    private ExternalStorage() {

    }

    /**
     * app文件存放根目录名
     */
    private final static String APP_DIRECTORY_NAME = "neliveplayer/";

    synchronized public static ExternalStorage getInstance() {
        if (instance == null) {
            instance = new ExternalStorage();
        }
        return instance;
    }

    public void init(Context context, String sdkStorageRoot) {
        this.context = context;

        // 判断权限
        hasPermission = preCheckPermission(sdkStorageRoot);
        if (!hasPermission) {
            hasPermission = checkPermission();
        }

        if (!TextUtils.isEmpty(sdkStorageRoot)) {
            File dir = new File(sdkStorageRoot);
            if (!dir.exists()) {
                if (dir.getParentFile().exists()) {
                    dir.mkdir(); // dir父目录存在用mkDir
                } else {
                    dir.mkdirs(); // dir父目录不存在用mkDirs
                }
            }
            if (dir.exists() && !dir.isFile()) {
                this.sdkStorageRoot = sdkStorageRoot;
                if (!sdkStorageRoot.endsWith("/")) {
                    this.sdkStorageRoot = sdkStorageRoot + "/";
                }
            }
        }

        if (TextUtils.isEmpty(this.sdkStorageRoot)) {
            loadStorageState(context);
        }

        createSubFolders();
    }

    private void loadStorageState(Context context) {
        String externalPath = Environment.getExternalStorageDirectory().getPath();
        this.sdkStorageRoot = externalPath + "/" + context.getPackageName() + "/" + APP_DIRECTORY_NAME;
    }

    private void createSubFolders() {
        boolean result = true;
        File root = new File(sdkStorageRoot);
        if (root.exists() && !root.isDirectory()) {
            root.delete();
        }
        for (StorageType storageType : StorageType.values()) {
            result &= makeDirectory(sdkStorageRoot + storageType.getStoragePath());
        }
        if (result) {
            createNoMediaFile(sdkStorageRoot);
        }
    }

    /**
     * 创建目录
     *
     * @param path
     * @return
     */
    private boolean makeDirectory(String path) {
        File file = new File(path);
        boolean exist = file.exists();
        if (!exist) {
            exist = file.mkdirs();
        }
        return exist;
    }

    protected static String NO_MEDIA_FILE_NAME = ".nomedia";

    private void createNoMediaFile(String path) {
        File noMediaFile = new File(path + "/" + NO_MEDIA_FILE_NAME);
        try {
            if (!noMediaFile.exists()) {
                noMediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件全名转绝对路径（写）
     *
     * @param fileName 文件全名（文件名.扩展名）
     * @return 返回绝对路径信息
     */
    public String getWritePath(String fileName, StorageType fileType) {
        return pathForName(fileName, fileType, false, false);
    }

    private String pathForName(String fileName, StorageType type, boolean dir,
                               boolean check) {
        String directory = getDirectoryByDirType(type);
        StringBuilder path = new StringBuilder(directory);

        if (!dir) {
            path.append(fileName);
        }

        String pathString = path.toString();
        File file = new File(pathString);

        if (check) {
            if (file.exists()) {
                if ((dir && file.isDirectory())
                        || (!dir && !file.isDirectory())) {
                    return pathString;
                }
            }

            return "";
        } else {
            return pathString;
        }
    }

    /**
     * 返回指定类型的文件夹路径
     *
     * @param fileType
     * @return
     */
    public String getDirectoryByDirType(StorageType fileType) {
        return sdkStorageRoot + fileType.getStoragePath();
    }

    /**
     * 根据输入的文件名和类型，找到该文件的全路径。
     *
     * @param fileName
     * @param fileType
     * @return 如果存在该文件，返回路径，否则返回空
     */
    String getReadPath(String fileName, StorageType fileType) {
        if (TextUtils.isEmpty(fileName)) {
            return "";
        }

        return pathForName(fileName, fileType, false, true);
    }

    boolean isSdkStorageReady() {
        String externalRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (this.sdkStorageRoot.startsWith(externalRoot)) {
            return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        } else {
            return true;
        }
    }

    /**
     * 获取外置存储卡剩余空间
     *
     * @return
     */
    public long getAvailableExternalSize() {
        return getResidualSpace(sdkStorageRoot);
    }

    /**
     * 获取目录剩余空间
     *
     * @param directoryPath
     * @return
     */
    private long getResidualSpace(String directoryPath) {
        try {
            StatFs sf = new StatFs(directoryPath);
            long blockSize = sf.getBlockSize();
            long availCount = sf.getAvailableBlocks();
            long availCountByte = availCount * blockSize;
            return availCountByte;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    String getSdkStorageRoot() {
        return sdkStorageRoot;
    }

    /**
     * SD卡存储权限检查
     */
    private boolean checkPermission() {
        if (context == null) {
            Log.e(TAG, "checkPermission context null");
            return false;
        }

        if(!checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Log.e(TAG, "without permission to access storage");
            return false;
        }

        return true;
    }

    public boolean checkPermission(Context context, String permissionName) {
        if (context == null || TextUtils.isEmpty(permissionName)) {
            return false;
        }
        PackageManager pm = context.getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission(permissionName, context.getApplicationInfo().packageName);
    }

    /**
     * special root dir, no need to request permission
     */
    private boolean preCheckPermission(final String sdkStorageRoot) {
        if (!TextUtils.isEmpty(sdkStorageRoot)) {
            try {
                File file = context.getExternalCacheDir();
                if (file != null && sdkStorageRoot.startsWith(file.getCanonicalPath())) {
                    Log.i(TAG, "use external cache dir!");
                    return true;
                }

                file = context.getExternalFilesDir(null);
                if (file != null && sdkStorageRoot.startsWith(file.getCanonicalPath())) {
                    Log.i(TAG, "use external files dir!");
                    return true;
                }

                if (sdkStorageRoot.startsWith(context.getCacheDir().getCanonicalPath()) ||
                        sdkStorageRoot.startsWith(context.getFilesDir().getCanonicalPath())) {
                    Log.i(TAG, "use internal cache dir!");
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "use public storage, path=" + sdkStorageRoot);
        return false;
    }

    /**
     * 有效性检查
     */
    public boolean checkStorageValid() {
        if (hasPermission) {
            return true; // M以下版本&授权过的M版本不需要检查
        }

        hasPermission = checkPermission(); // 检查是否已经获取权限了
        if (hasPermission) {
            Log.i(TAG, "get permission to access storage");

            // 已经重新获得权限，那么重新检查一遍初始化过程
            createSubFolders();
        }
        return hasPermission;
    }
}
