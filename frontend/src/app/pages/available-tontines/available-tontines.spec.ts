import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AvailableTontines } from './available-tontines';

describe('AvailableTontines', () => {
  let component: AvailableTontines;
  let fixture: ComponentFixture<AvailableTontines>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AvailableTontines],
    }).compileComponents();

    fixture = TestBed.createComponent(AvailableTontines);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
