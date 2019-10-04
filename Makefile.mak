PROJECT = GWMS

build: create_dir copy_python_files zip

rebuild: clean install_dependency build

create_dir:
		mkdir -p ./package/tmp/

clean:
		rm -rf ./package
		rm -rf ./*.zip

install_dependency:
		pip install aws_kinesis_agg==1.1.0 --no-cache-dir -t ./package/tmp/
		pip install pyOpenSSL --no-cache-dir -t ./package/tmp/
		pip install cryptography --no-cache-dir -t ./package/tmp/
		pip install requests --no-cache-dir -t ./package/tmp/
		pip install asn1crypto --no-cache-dir -t ./package/tmp/
		pip install requests-aws --no-cache-dir -t ./package/tmp/

zip:
		cd ./package/tmp && zip -r ../../$(PROJECT).zip * .[^.]*

		
		
		
		