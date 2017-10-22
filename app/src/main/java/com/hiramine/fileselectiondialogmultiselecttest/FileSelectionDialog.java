/*
 * Copyright 2017 Nobuki HIRAMINE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hiramine.fileselectiondialogmultiselecttest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

public class FileSelectionDialog implements AdapterView.OnItemClickListener, View.OnClickListener
{
	static public class FileInfo implements Comparable<FileInfo>
	{
		private String  m_strName;    // 表示名
		private File    m_file;    // ファイルオブジェクト
		private boolean m_bSelected;    // 選択状態


		// コンストラクタ
		public FileInfo( String strName, File file )
		{
			m_strName = strName;
			m_file = file;
		}

		public String getName()
		{
			return m_strName;
		}

		public File getFile()
		{
			return m_file;
		}

		public boolean isSelected()
		{
			return m_bSelected;
		}

		public void setSelected( boolean bSelected )
		{
			m_bSelected = bSelected;
		}

		// 比較
		public int compareTo( FileInfo another )
		{
			// ディレクトリ < ファイル の順
			if( m_file.isDirectory() && !another.getFile().isDirectory() )
			{
				return -1;
			}
			if( !m_file.isDirectory() && another.getFile().isDirectory() )
			{
				return 1;
			}

			// ファイル同士、ディレクトリ同士の場合は、ファイル名（ディレクトリ名）の大文字小文字区別しない辞書順
			return m_file.getName().toLowerCase().compareTo( another.getFile().getName().toLowerCase() );
		}
	}

	static public class FileInfoArrayAdapter extends BaseAdapter
	{
		private Context        m_context;
		private List<FileInfo> m_listFileInfo; // ファイル情報リスト

		// コンストラクタ
		public FileInfoArrayAdapter( Context context, List<FileInfo> list )
		{
			super();
			m_context = context;
			m_listFileInfo = list;
		}

		@Override
		public int getCount()
		{
			return m_listFileInfo.size();
		}

		@Override
		public FileInfo getItem( int position )
		{
			return m_listFileInfo.get( position );
		}

		@Override
		public long getItemId( int position )
		{
			return position;
		}

		static class ViewHolder
		{
			TextView textviewFileName;
			TextView textviewFileSize;
		}

		// 一要素のビューの生成
		@Override
		public View getView( int position, View convertView, ViewGroup parent )
		{
			ViewHolder viewHolder;
			if( null == convertView )
			{
				// レイアウト
				LinearLayout layout = new LinearLayout( m_context );
				layout.setOrientation( LinearLayout.VERTICAL );
				layout.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
				// ファイル名テキスト
				TextView textviewFileName = new TextView( m_context );
				textviewFileName.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 24 );
				layout.addView( textviewFileName, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );
				// ファイルサイズテキスト
				TextView textviewFileSize = new TextView( m_context );
				textviewFileSize.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 12 );
				layout.addView( textviewFileSize, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT ) );

				convertView = layout;
				viewHolder = new ViewHolder();
				viewHolder.textviewFileName = textviewFileName;
				viewHolder.textviewFileSize = textviewFileSize;
				convertView.setTag( viewHolder );
			}
			else
			{
				viewHolder = (ViewHolder)convertView.getTag();
			}

			FileInfo fileinfo = m_listFileInfo.get( position );
			if( fileinfo.getFile().isDirectory() )
			{ // ディレクトリの場合は、名前の後ろに「/」を付ける
				viewHolder.textviewFileName.setText( fileinfo.getName() + "/" );
				viewHolder.textviewFileSize.setText( "(directory)" );
			}
			else
			{
				viewHolder.textviewFileName.setText( fileinfo.getName() );
				viewHolder.textviewFileSize.setText( String.valueOf( fileinfo.getFile().length() / 1024 ) + " [KB]" );
			}

			// 選択行と非選択行
			if( fileinfo.isSelected() )
			{ // 選択行：青背景、白文字
				convertView.setBackgroundColor( Color.BLUE );
				viewHolder.textviewFileName.setTextColor( Color.WHITE );
				viewHolder.textviewFileSize.setTextColor( Color.WHITE );
			}
			else
			{ // 非選択行：白背景、黒文字
				convertView.setBackgroundColor( Color.WHITE );
				viewHolder.textviewFileName.setTextColor( Color.BLACK );
				viewHolder.textviewFileSize.setTextColor( Color.BLACK );
			}

			return convertView;
		}

		// 一番上の選択されたアイテムの位置の取得
		public int getFirstSelectedItemPosition()
		{
			ListIterator<FileInfo> it = m_listFileInfo.listIterator();
			while( it.hasNext() )
			{
				FileInfo fileinfo = it.next();
				if( fileinfo.isSelected() )
				{
					return it.nextIndex() - 1;
				}
			}
			return -1;
		}

		// positionで指定したアイテムの次の選択されたアイテムの位置の取得
		public int getNextSelectedItemPosition( int iPosition )
		{
			ListIterator<FileInfo> it = m_listFileInfo.listIterator( iPosition );
			// 一つ進める
			it.next();
			while( it.hasNext() )
			{
				FileInfo fileinfo = it.next();
				if( fileinfo.isSelected() )
				{
					return it.nextIndex() - 1;
				}
			}
			return -1;
		}
	}

	private Context              m_contextParent;    // 呼び出し元
	private OnFileSelectListener m_listener;    // 結果受取先
	private AlertDialog          m_dialog;    // ダイアログ
	private FileInfoArrayAdapter m_fileinfoarrayadapter; // ファイル情報配列アダプタ
	private String[]             m_astrExt;                // フィルタ拡張子配列
	private Button               m_button_ok;                // 「OK」ボタン

	// コンストラクタ
	public FileSelectionDialog( Context context, OnFileSelectListener listener, String strExt )
	{
		m_contextParent = context;
		m_listener = listener;

		// 拡張子フィルタ
		if( null != strExt )
		{
			StringTokenizer tokenizer   = new StringTokenizer( strExt, "; " );
			int             iCountToken = 0;
			while( tokenizer.hasMoreTokens() )
			{
				tokenizer.nextToken();
				iCountToken++;
			}
			if( 0 != iCountToken )
			{
				m_astrExt = new String[iCountToken];
				tokenizer = new StringTokenizer( strExt, "; " );
				iCountToken = 0;
				while( tokenizer.hasMoreTokens() )
				{
					m_astrExt[iCountToken] = tokenizer.nextToken();
					iCountToken++;
				}
			}
		}
	}

	// ダイアログの作成と表示
	public void show( File fileDirectory )
	{
		// タイトル
		String strTitle = fileDirectory.getAbsolutePath();

		// リストビュー
		ListView listview = new ListView( m_contextParent );
		listview.setScrollingCacheEnabled( false );
		listview.setOnItemClickListener( this );
		// ファイルリスト
		File[]         aFile        = fileDirectory.listFiles( getFileFilter() );
		List<FileInfo> listFileInfo = new ArrayList<>();
		if( null != aFile )
		{
			for( File fileTemp : aFile )
			{
				listFileInfo.add( new FileInfo( fileTemp.getName(), fileTemp ) );
			}
			Collections.sort( listFileInfo );
		}
		// 親フォルダに戻るパスの追加
		if( null != fileDirectory.getParent() )
		{
			listFileInfo.add( 0, new FileInfo( "..", new File( fileDirectory.getParent() ) ) );
		}
		m_fileinfoarrayadapter = new FileInfoArrayAdapter( m_contextParent, listFileInfo );
		listview.setAdapter( m_fileinfoarrayadapter );

		AlertDialog.Builder builder = new AlertDialog.Builder( m_contextParent );
		builder.setTitle( strTitle );
		builder.setNegativeButton( "Cancel", null );
		builder.setPositiveButton( "OK", null );
		builder.setView( listview );
		m_dialog = builder.show();

		// Builder#setPositiveButton() でリスナーを指定した場合、ダイアログは閉じてしまう。
		// ファイルを一つも選択していない場合は、ダイアログを閉じないようにしたいので、
		// Button#setOnClickListener() でリスナーを指定する。
		m_button_ok = m_dialog.getButton( DialogInterface.BUTTON_POSITIVE );
		m_button_ok.setOnClickListener( this );
	}

	@Override
	public void onClick( View v )
	{
		if( v.getId() == m_button_ok.getId() )
		{
			// ファイルが選択されているか
			int iPosition_first = m_fileinfoarrayadapter.getFirstSelectedItemPosition();
			int iPosition = iPosition_first;
			int iCount = 0;
			while( -1 != iPosition )
			{
				iPosition = m_fileinfoarrayadapter.getNextSelectedItemPosition( iPosition );
				iCount++;
			}
			if( 0 == iCount )
			{	// ひとつも選択されていない
				Toast.makeText( m_contextParent, "No file selected.", Toast.LENGTH_SHORT ).show();
			}
			else
			{	// ひとつ以上選択されている
				File[] aFile = new File[iCount];
				iPosition = iPosition_first;
				iCount = 0;
				while( -1 != iPosition )
				{
					aFile[iCount] = m_fileinfoarrayadapter.getItem( iPosition ).getFile();
					iPosition = m_fileinfoarrayadapter.getNextSelectedItemPosition( iPosition );
					iCount++;
				}
				m_dialog.dismiss();
				m_dialog = null;
				// リスナーのハンドラを呼び出す
				m_listener.onFileSelect( aFile );
			}
		}
	}

	// ListView内の項目をクリックしたときの処理
	public void onItemClick( AdapterView<?> parent, View view, int position, long id )
	{
		FileInfo fileinfo = m_fileinfoarrayadapter.getItem( position );

		if( fileinfo.getFile().isDirectory() )
		{ // ディレクトリをクリックしたときは、ダイアログを閉じて、ダイアログの再作成
			if( null != m_dialog )
			{
				m_dialog.dismiss();
				m_dialog = null;
			}
			show( fileinfo.getFile() );
		}
		else
		{ // ファイルをクリックしたときは、選択状態の反転と、クリック行の表示更新
			fileinfo.setSelected( !fileinfo.isSelected() );
			parent.getAdapter().getView( position, view, parent );
		}
	}

	// 選択したファイルの情報を取り出すためのリスナーインターフェース
	public interface OnFileSelectListener
	{
		// ファイルが選択されたときに呼び出される関数
		void onFileSelect( File[] aFile );
	}

	// FileFilterオブジェクトの生成
	private FileFilter getFileFilter()
	{
		return new FileFilter()
		{
			public boolean accept( File file )
			{
				if( null == m_astrExt )
				{ // フィルタしない
					return true;
				}
				if( file.isDirectory() )
				{ // ディレクトリのときは、true
					return true;
				}
				for( String strTemp : m_astrExt )
				{
					if( file.getName().toLowerCase().endsWith( "." + strTemp ) )
					{
						return true;
					}
				}
				return false;
			}
		};
	}
}



