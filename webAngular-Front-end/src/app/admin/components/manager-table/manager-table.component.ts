import { CommonModule, DatePipe } from '@angular/common';
import { AfterViewInit, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { Router } from '@angular/router';
import { ManagerService } from 'src/app/core/services/manager.service';
import { Manager } from 'src/app/shared/models/manager';
import { DialogUpdateManagerComponent } from "./dialog-update-manager/dialog-update-manager.component";

@Component({
  selector: 'app-manager-table',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatInputModule,
    MatButtonModule,
    MatFormFieldModule,
    FormsModule,
    MatIconModule,
    DatePipe,
    DialogUpdateManagerComponent
],
  templateUrl: './manager-table.component.html',
  styleUrls: ['./manager-table.component.scss']
})
export class ManagerTableComponent implements OnChanges, AfterViewInit {
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @Input() managers: Manager[] = [];
  @Output() managerDeleted = new EventEmitter<void>();
  showDialog:boolean=false;
  selectedManager: Manager | null = null;

  
  dataSource: MatTableDataSource<Manager>;
  displayedColumns: string[] = ['avatar', 'nom', 'prenom', 'cin', 'email', 'actions'];
  showConfirmModal = false;
  managerToDelete: number | null = null;

  constructor(private managerService: ManagerService,private route:Router) {
    this.dataSource = new MatTableDataSource<Manager>([]);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['managers']) {
      try {
        console.log('ahhahahhahahha',this.managers)
        this.dataSource.data = this.managers;
        this.dataSource.paginator = this.paginator;
        this.dataSource._updateChangeSubscription();
      } catch (error) {
        console.error('Error updating data source:', error);
      }
    }
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  deleteManager(managerId: number): void {
    this.managerToDelete = managerId;
    this.showConfirmModal = true;
  }

  cancelDelete(): void {
    this.showConfirmModal = false;
    this.managerToDelete = null;
  }

  confirmDelete(): void {
    if (this.managerToDelete !== null) {
      this.managerService.delete(this.managerToDelete).subscribe({
        next: (response) => {
          console.log('Manager deleted successfully', response);
          this.dataSource.data = this.dataSource.data.filter((manager) => manager.id !== this.managerToDelete);
          this.dataSource._updateChangeSubscription();
          this.showConfirmModal = false;
          this.managerToDelete = null;
          this.managerDeleted.emit();
        },
        error: (error) => {
          console.error('There was an error deleting the manager!', error);
          this.showConfirmModal = false;
          this.managerToDelete = null;
        }
      });
    }
  }

  

  viewProfil(manager: Manager) {
    this.route.navigateByUrl(`admin/manager/${manager.id}`)
  }
  modifierManager(manager: Manager) {
    this.selectedManager = manager;
    this.showDialog = true;
  }

  onDialogClose() {
    this.showDialog = false;
    this.selectedManager = null;
  }
  onManagerUpdated(event: { success: boolean, message: string }) {
    if (event.success) {
      // Refresh the manager list or update the local data
      this.managerService.getAllManagers().subscribe(
        managers => {
          this.managers = managers;
          this.dataSource.data = this.managers;
          this.dataSource._updateChangeSubscription();
        }
      );
    }
    // Show feedback message
    // You can use a snackbar or any other method to show the message
    console.log(event.message);
  }
}