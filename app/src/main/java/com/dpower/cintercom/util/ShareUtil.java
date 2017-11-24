package com.dpower.cintercom.util;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.Toast;

import com.dpower.cintercom.R;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

/**
 * 分享工具类（QQ分享、微信分享）
 */
public class ShareUtil {
	public static final int QQ = 1;
	public static final int WECHAT = 2;
	// QQ Appid修改时需要同时修改AndroidManifest
	private static final String APPID_QQ = "1106250669";
	private static final String APPID_WECHAT = "wxb828e879f5fe73ea";
	private static final int THUMB_SIZE = 150;
	private Tencent mTencent;
	private IWXAPI mWechat;
	private Activity mActivity;
	private Context mContext;
	private int type =0;
	private String title;
	private String content;

	public ShareUtil(Activity mActivity, int type) {
		this.mActivity = mActivity;
		mContext = mActivity;
		if (type ==  QQ) {
			this.type = type;
			mTencent = Tencent.createInstance(APPID_QQ, mContext);
		}
		if (type == WECHAT) {
			this.type = type;
			final String appId = APPID_WECHAT;
			mWechat = WXAPIFactory.createWXAPI(mContext, appId, true);
			mWechat.registerApp(appId);
		}
		title = mContext.getString(R.string.share_title);
		content = mContext.getString(R.string.share_content);
	}

	public boolean isWeChatAppInstalled() {
		if (type == WECHAT) {
			return mWechat.isWXAppInstalled();
		}
		return false;
	}

	// 分享网址
	public void shareUrl(String url, String img_url) {
		if (type == 0)
			return;
		if (type == QQ) {
			// 分享到QQ
			final Bundle params = new Bundle();
			params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
			params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
			params.putString(QQShare.SHARE_TO_QQ_SUMMARY, content);
			params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
			params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, img_url);
			mTencent.shareToQQ(mActivity, params, new BaseUiListener());
			return;
		}
		if (type == WECHAT) {
			// 分享到微信
			WXWebpageObject webpage = new WXWebpageObject();
			webpage.webpageUrl = url;

			WXMediaMessage msg = new WXMediaMessage(webpage);
			msg.title = title;
			msg.description = content;
			Bitmap thumb = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
			msg.thumbData = bmpToByteArray(thumb, true);

			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("webpage");
			req.message = msg;
			req.scene =  SendMessageToWX.Req.WXSceneSession;

			mWechat.sendReq(req);
			// -------------------------------------------------------
//			String text = url;
//			if (text == null || text.length() == 0) {
//				return;
//			}
//
//			// 初始化一个WXTextObject对象
//			WXTextObject textObj = new WXTextObject();
//			textObj.text = text;
//
//			// 用WXTextObject对象初始化一个WXMediaMessage对象
//			WXMediaMessage msg = new WXMediaMessage();
//			msg.mediaObject = textObj;
//			// 发送文本类型的消息时，title字段不起作用
//			// msg.title = "Will be ignored";
//			msg.description = text;
//
//			// 构造一个Req
//			SendMessageToWX.Req req = new SendMessageToWX.Req();
//			req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
//			req.message = msg;
//			req.scene = SendMessageToWX.Req.WXSceneSession;
//
//			// 调用api接口发送数据到微信
//			mWechat.sendReq(req);
			return;
		}
	}

	// 分享图片
	public void sharePic(String path) {
		if (type == 0)
			return;
		if (type == QQ) {
			// 分享到QQ
			Bundle params = new Bundle();
			params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
			params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path);
			mTencent.shareToQQ(mActivity, params, new BaseUiListener());
			return;
		}
		if (type == WECHAT) {
			// 分享到微信
			File file = new File(path);
			if (!file.exists()) {
				Toast.makeText(mContext, mContext.getString(R.string.send_img_file_not_exist), Toast.LENGTH_LONG).show();
				return;
			}
			// 初始化WXImageObject和WXMediaMessage对象
			WXImageObject imgObj = new WXImageObject();
			imgObj.setImagePath(path);
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = imgObj;

			//  设置缩略图
			Bitmap bmp = BitmapFactory.decodeFile(path);
			Bitmap thumBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
			bmp.recycle();
			msg.thumbData = bmpToByteArray(thumBmp, true);

			// 构造一个Req
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("img"); // transaction字段用于唯一标识一个请求
			req.message = msg;
			req.scene = SendMessageToWX.Req.WXSceneSession;
			mWechat.sendReq(req);
			return;
		}
	}

	private class BaseUiListener implements IUiListener {
		@Override
		public void onCancel() {

		}

		@Override
		public void onComplete(Object arg0) {

		}

		@Override
		public void onError(UiError arg0) {
			Toast.makeText(mContext, R.string.share_error, Toast.LENGTH_SHORT).show();
		}
	}

	public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
		int i;
		int j;
		if (bmp.getHeight() > bmp.getWidth()) {
			i = bmp.getWidth();
			j = bmp.getWidth();
		} else {
			i = bmp.getHeight();
			j = bmp.getHeight();
		}

		Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
		Canvas localCanvas = new Canvas(localBitmap);

		while (true) {
			localCanvas.drawBitmap(bmp, new Rect(0, 0, i, j), new Rect(0, 0,i, j), null);
			if (needRecycle)
				bmp.recycle();
			ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
			localBitmap.compress(Bitmap.CompressFormat.JPEG, 40,
					localByteArrayOutputStream);
			localBitmap.recycle();
			byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
			try {
				localByteArrayOutputStream.close();
				return arrayOfByte;
			} catch (Exception e) {
				//F.out(e);
			}
			i = bmp.getHeight();
			j = bmp.getHeight();
		}
	}

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
}