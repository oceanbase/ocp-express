%define _unpackaged_files_terminate_build 0
%define __jar_repack 0

Name: ocp-express
Version: 4.2.0
Release: %(echo $RELEASE)
Summary: OCP Express
License: Mulan PubL v2
URL: https://www.oceanbase.com
Group: Applications/Databases
Packager: OceanBase Inc
Vendor: OceanBase Inc
autoprov: yes
autoreq: yes
Prefix: /home/admin/ocp-express

%description
Lightweight OceanBase management tool.

%define _prefix /home/admin/ocp-express

%prep
echo "Installing ocp-express now"

%install

mkdir -p $RPM_BUILD_ROOT/%{_prefix}/lib/
mkdir -p $RPM_BUILD_ROOT/%{_prefix}/conf/

cp %{_workspace}/server/target/ocp-express-server*.jar $RPM_BUILD_ROOT/%{_prefix}/lib/ocp-express-server.jar
cp %{_workspace}/rpm/ocp-express-version.txt $RPM_BUILD_ROOT/%{_prefix}/INFO.txt
cp %{_workspace}/rpm/ocp-express-config-mapper.yaml $RPM_BUILD_ROOT/%{_prefix}/conf/

%files
%dir %{_prefix}
  %{_prefix}/lib/ocp-express-server.jar
  %{_prefix}/INFO.txt
  %{_prefix}/conf
