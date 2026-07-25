export const environment = {
  // Relative path: the API is reached through the same origin that serves the app
  // (nginx proxies /ddl/api to the backend, `ng serve` does the same via proxy.conf.json).
  apiUrl: '/ddl/api',
  // How many past years (in addition to the current one) the year dropdowns offer.
  yearsBack: 10,
};
