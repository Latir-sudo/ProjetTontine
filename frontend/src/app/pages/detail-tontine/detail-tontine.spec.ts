import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailTontine } from './detail-tontine';

describe('DetailTontine', () => {
  let component: DetailTontine;
  let fixture: ComponentFixture<DetailTontine>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailTontine],
    }).compileComponents();

    fixture = TestBed.createComponent(DetailTontine);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
