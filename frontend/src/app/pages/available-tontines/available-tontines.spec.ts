import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { AvailableTontines } from './available-tontines';

describe('AvailableTontines', () => {
  let component: AvailableTontines;
  let fixture: ComponentFixture<AvailableTontines>;
  let apiService: any;
  let authService: any;

  beforeEach(async () => {
    apiService = {
      get: async () => [],
      post: async () => ({})
    };
    authService = {
      currentUser: () => ({ id: 7 })
    };

    await TestBed.configureTestingModule({
      imports: [AvailableTontines, RouterTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(AvailableTontines);
    component = fixture.componentInstance;
    (component as any).apiService = apiService;
    (component as any).authService = authService;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show feedback when a request has already been sent', async () => {
    let postCallCount = 0;
    apiService.get = async () => [{ idUser: 7, statut: 'ATTENTE' }];
    apiService.post = async () => {
      postCallCount += 1;
      return {};
    };

    await component.postulerAdhesion(12);

    expect(component['feedbackMessage']).toContain('déjà été envoyée');
    expect(component['feedbackType']).toBe('info');
    expect(postCallCount).toBe(0);
  });
});
