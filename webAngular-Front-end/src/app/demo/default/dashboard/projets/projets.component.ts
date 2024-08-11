// projets.component.ts

import { Component, OnDestroy, OnInit } from '@angular/core';
import { ProjetTableComponent } from '../projet-table/projet-table.component';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Projet } from 'src/app/model/projet';
import { ProjetService } from 'src/app/services/projet.service';
import { BehaviorSubject, Observable, Subscription, tap } from 'rxjs';
import { LoadingService } from 'src/app/services/loading.service';
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../../theme/shared/shared.module';
import { DialogProjetComponent } from '../dialog-projet/dialog-projet.component';

@Component({
  selector: 'app-projets',
  standalone: true,
  imports: [ProjetTableComponent, CommonModule, SharedModule, RouterLink, DialogProjetComponent],
  templateUrl: './projets.component.html',
  styleUrl: './projets.component.scss'
})
export class ProjetsComponent implements OnInit, OnDestroy {
  private projetSubject = new BehaviorSubject<Projet[]>([]);
  private subscription: Subscription = new Subscription();

  nomEquipe: string = '';
  equipeId: string = '';
  projets: Projet[] = [];

  projets$ = this.projetSubject.asObservable();
  loading$: Observable<boolean>;
  error$: Observable<boolean>;

  showDialog = false;

  constructor(
    private stateService: LoadingService,
    private route: ActivatedRoute,
    private projetService: ProjetService,
  ) {
    this.loading$ = this.stateService.loading$;
    this.error$ = this.stateService.error$;
  }

  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.equipeId = params['id'];
      this.nomEquipe = params['nomEquipe'];
      this.loadProjets();
    });
  }

  loadProjets() {
    const loadedData$ = this.stateService.loadData(
      this.projetService.getProjetsByTeamId(this.equipeId).pipe(tap((projets) => console.log('prjts: prjts received', projets))),
      400
    );

    this.subscription.add(
      loadedData$.subscribe(
        (prjts) => {
          console.log('prjts: Updating prjts', prjts);
          this.projetSubject.next(prjts || []);
        },
        (error) => console.error('prjts: Error in prjts subscription', error)
      )
    );
  }

  ajouterProjet() {
    this.showDialog = true;
  }

  handleClose() {
    this.showDialog = false;
  }

  onProjectAdded(newProject: Projet) {
    this.loadProjets(); // Reload projects after adding a new one
    this.showDialog = false;
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }
}