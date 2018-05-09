package com.example.dell.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.dell.inventoryapp.data.ProdContract.ProductEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ProductAdapter mAdapter;

    public static final int PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button increaseButton = (Button) findViewById(R.id.increase_quan_button_view);
        increaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addProductIntent = new Intent(MainActivity.this, EditorAct.class);
                startActivity(addProductIntent);
            }
        });
        ListView listView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        Button deleteallButton = (Button) findViewById(R.id.delete_all);
        deleteallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialogBox();
            }
        });

        mAdapter = new ProductAdapter(this, null);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MainActivity.this, EditorAct.class);

                Uri currentProduct = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                intent.setData(currentProduct);

                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {ProductEntry._ID, ProductEntry.COLUMN_PRODUCT_NAME, ProductEntry.COLUMN_PRODUCT_PRICE, ProductEntry.COLUMN_PRODUCT_QUANTITY, ProductEntry.COLUMN_PRODUCT_CODE};
        return new CursorLoader(this, ProductEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private void showDeleteConfirmationDialogBox() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.deletedialogmsg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void deleteProduct() {
        getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
    }


}
