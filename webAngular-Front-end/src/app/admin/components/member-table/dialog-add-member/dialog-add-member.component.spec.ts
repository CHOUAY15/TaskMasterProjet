import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DialogAddMemberComponent } from './dialog-add-member.component';

describe('DialogAddMemberComponent', () => {
  let component: DialogAddMemberComponent;
  let fixture: ComponentFixture<DialogAddMemberComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DialogAddMemberComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DialogAddMemberComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
