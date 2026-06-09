import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateTontine } from './create-tontine';

describe('CreateTontine', () => {
  let component: CreateTontine;
  let fixture: ComponentFixture<CreateTontine>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateTontine],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateTontine);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
