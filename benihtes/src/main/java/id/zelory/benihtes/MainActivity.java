/*
 * Copyright (c) 2015 Zetra.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package id.zelory.benihtes;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.trello.rxlifecycle.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import id.zelory.benih.BenihActivity;
import id.zelory.benih.util.BenihBus;
import id.zelory.benih.util.BenihWorker;
import id.zelory.benih.view.BenihRecyclerView;
import id.zelory.benihtes.adapter.BeritaRecyclerAdapter;
import id.zelory.benihtes.controller.BeritaController;
import id.zelory.benihtes.model.Berita;
import timber.log.Timber;

public class MainActivity extends BenihActivity implements BeritaController.Presenter
{
    private BeritaController beritaController;
    @Bind(R.id.recycler_view) BenihRecyclerView recyclerView;
    private BeritaRecyclerAdapter adapter;

    @Override
    protected int getActivityView()
    {
        return R.layout.activity_main;
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState)
    {
        BenihBus.pluck()
                .receive()
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(o -> Timber.d(o.toString()), throwable -> Timber.d(throwable.getMessage()));

        setUpAdapter();
        setUpRecyclerView();
        setUpController(savedInstanceState);

        beritaController.doSomeThing();

        doSomeDummyThread();
    }

    private void setUpAdapter()
    {
        adapter = new BeritaRecyclerAdapter(this);
        adapter.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(this, BacaActivity.class);
            intent.putParcelableArrayListExtra("data", (ArrayList<Berita>) adapter.getData());
            intent.putExtra("pos", position);
            startActivity(intent);
        });
        adapter.setOnLongItemClickListener((view, position) -> adapter.remove(position));
    }

    private void setUpRecyclerView()
    {
        recyclerView.setUpAsList();
        recyclerView.setAdapter(adapter);
    }

    private void setUpController(Bundle bundle)
    {
        if (beritaController == null)
        {
            beritaController = new BeritaController(this);
        }

        if (bundle != null)
        {
            beritaController.loadState(bundle);
        } else
        {
            beritaController.loadListBerita();
        }
    }

    private void doSomeDummyThread()
    {
        for (int i = 0; i < 10; i++)
        {
            final int thread = i;
            BenihWorker.pluck()
                    .doInComputation(() -> {
                        for (int j = 0; j < 10000; j++)
                        {
                            for (int k = 0; k < j; k++)
                            {
                                int a = j;
                                int b = k;
                                int c = a * k + b * j;
                                c = c / (j - k);
                            }
                        }
                    })
                    .compose(bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(o -> Timber.d("Worker " + thread + " is done."), throwable -> Timber.d(throwable.getMessage()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        BenihBus.pluck().send("onCreateOptionMenu()");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        BenihBus.pluck().send("onOptionsMenuSelected");
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SecondActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void showListBerita(List<Berita> listBerita)
    {
        adapter.add(listBerita);
    }

    @Override
    public void showBerita(Berita berita)
    {

    }

    @Override
    public void showSomeThing()
    {
        adapter.remove(3);
        Toast.makeText(this, "WOWWOWOWOWO", Toast.LENGTH_LONG).show();
    }

    @Override
    public void showError(Throwable throwable)
    {
        Snackbar.make(recyclerView, "Something wrong!", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState)
    {
        beritaController.saveState(outState);
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
